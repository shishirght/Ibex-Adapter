package com.eh.digitalpathology.ibex.controller;


import com.eh.digitalpathology.ibex.model.SlideScanProgressEvent;
import com.eh.digitalpathology.ibex.service.BarcodeStudyInfo;
import com.eh.digitalpathology.ibex.service.KafkaNotificationProducer;
import com.eh.digitalpathology.ibex.util.SignatureUtils;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping( "/api/files" )
public class FileDownloadController {

    private static final Logger logger = LoggerFactory.getLogger( FileDownloadController.class );

    private final Storage storage;
    private final SignatureUtils signatureUtils;
    private final KafkaNotificationProducer kafkaNotificationProducer;
    private final BarcodeStudyInfo barcodeStudyInfo;

    public FileDownloadController (Storage storage, SignatureUtils signatureUtils, KafkaNotificationProducer kafkaNotificationProducer, BarcodeStudyInfo barcodeStudyInfo) {
        this.storage = storage;
        this.signatureUtils = signatureUtils;
        this.kafkaNotificationProducer = kafkaNotificationProducer;
        this.barcodeStudyInfo = barcodeStudyInfo;
    }

    @GetMapping("/download")
    public void download(
            @RequestParam String bucket,
            @RequestParam String object,
            @RequestParam(required = false) Long generation,
            @RequestParam long expires,
            @RequestParam String sig,
            HttpServletResponse response
    ) throws Exception {
        final String path = "/api/files/download";
        final long t0 = System.currentTimeMillis();
        logger.info("download :: start :: bucket={} object={} generation={} expires={} sig.len={}",
                bucket, object, generation, expires, (sig == null ? 0 : sig.length()));

        String fileName = null;

        try {
            // 1) Validate signed URL (early returns to avoid nesting)
            final String canonical = signatureUtils.canonical("GET", path, bucket, object, generation, expires);
            logger.info("download :: canonical :: {}", canonical);

            if (signatureUtils.isExpired(expires)) {
                logger.warn("download :: link expired :: now={} expires={}", System.currentTimeMillis() / 1000, expires);
                respondWithText(response, HttpStatus.GONE, "Link expired");
                return;
            }

            if (!signatureUtils.verify(sig, canonical)) {
                logger.info("download :: signature.verify :: false");
                respondWithText(response, HttpStatus.FORBIDDEN, "Invalid signature");
                return;
            }
            logger.info("download :: signature.verify :: true");

            // 2) Fetch blob (early return if not found)
            final Blob blob = getBlob(bucket, object, generation);
            if (blob == null) {
                logger.warn("download :: blob not found :: bucket={} object={} generation={}", bucket, object, generation);
                respondWithText(response, HttpStatus.NOT_FOUND, "Object not found");
                return;
            }

            // 3) Prepare headers
            fileName = extractFileName(blob.getName());
            prepareDownloadHeaders(response, blob, fileName);

            // 4) Stream
            streamBlobToResponse(blob, response, t0, fileName);

        } catch (Exception e) {
            handleError(response, e, fileName);
        }
    }

    /* ====================== helpers (reduced branching & isolated concerns) ====================== */

    private Blob getBlob(String bucket, String object, Long generation) {
        final long t1 = System.currentTimeMillis();
        final BlobId blobId = (generation != null) ? BlobId.of(bucket, object, generation) : BlobId.of(bucket, object);
        final Blob blob = storage.get(blobId);
        final long t2 = System.currentTimeMillis();
        logger.info("download :: storage.get :: {} ms", (t2 - t1));
        return blob;
    }

    private String extractFileName(String blobName) {
        final int idx = blobName.lastIndexOf('/');
        return (idx >= 0) ? blobName.substring(idx + 1) : blobName;
    }

    private void prepareDownloadHeaders(HttpServletResponse response, Blob blob, String fileName) {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setHeader("Content-Length", String.valueOf(blob.getSize()));
        if (blob.getEtag() != null) {
            response.setHeader("ETag", blob.getEtag());
        }
        response.setBufferSize(APP_BUFFER_SIZE);
        response.setContentLengthLong(blob.getSize());
    }

    private void streamBlobToResponse(Blob blob, HttpServletResponse response, long t0, String fileName) throws IOException {
        // Using 8MB read chunks from GCS and 16MB write buffer to client (adjust constants if desired)
        try (ReadChannel reader = blob.reader();
             BufferedOutputStream os = new BufferedOutputStream(response.getOutputStream(), APP_BUFFER_SIZE)) {

            reader.setChunkSize(GCS_RPC_CHUNK_SIZE);

            byte[] buf = new byte[APP_BUFFER_SIZE];
            long total = 0L;

            ByteBuffer buffer = ByteBuffer.allocate(APP_BUFFER_SIZE);
            int read;
            while ((read = reader.read(buffer)) >= 0) {
                buffer.flip();
                buffer.get(buf, 0, read);
                os.write(buf, 0, read);
                total += read;
                buffer.clear();
            }
            os.flush();
            logger.info("download :: streamed :: {} bytes in {} ms for file {}", total, (System.currentTimeMillis() - t0), fileName);
        }
    }

    private void respondWithText(HttpServletResponse response, HttpStatus status, String message) {
        response.setStatus(status.value());
        try {
            writeText(response, message);
        } catch (Exception ignore) {
            // Ignore
        }
    }

    private void handleError(HttpServletResponse response, Exception e, String fileName) {
        logger.error("download :: error :: {}", e.getMessage(), e);
        // Best-effort progress event notification
        tryNotifyDownloadFailure(fileName);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        try {
            writeText(response, "Server error: " + e.getMessage());
        } catch (Exception ignore) {
            // Ignore
        }
    }

    private void tryNotifyDownloadFailure(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return;
        }
        String studyId = fileName.replace(".zip", "");
        String barcode = barcodeStudyInfo.getBarcode(studyId);
        if (StringUtils.hasText(barcode)) {
            kafkaNotificationProducer.notifyScanProgress(new SlideScanProgressEvent(barcode, "ibex-files-download-failed"));
        } else {
            logger.error("Cannot publish scan progress failure event: barcode not found for studyId='{}' derived from file='{}'", studyId, fileName);
        }
    }

    /* ====================== tuning constants ====================== */

    // Note: The comments in your original suggested different sizes.
// Make them accurate & configurable (e.g., via @Value or application.yml).
    private static final int APP_BUFFER_SIZE = 16 * 1024 * 1024;   // 16MB write buffer to client
    private static final int GCS_RPC_CHUNK_SIZE = 8 * 1024 * 1024; // 8MB per GCS RPC (must be >= 256KB)
    private void writeText ( HttpServletResponse response, String msg ) throws IOException {
        response.setContentType( "text/plain; charset=UTF-8" );
        try ( ServletOutputStream os = response.getOutputStream( ) ) {
            os.write( msg.getBytes( StandardCharsets.UTF_8 ) );
            os.flush( );
        }
        response.flushBuffer( );
    }

}

