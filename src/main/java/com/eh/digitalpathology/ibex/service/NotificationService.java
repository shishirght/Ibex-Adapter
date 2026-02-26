package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.config.GcpConfig;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final IbexAdapterService ibexAdapterService;
    private final ExecutorService executorService;
    private final BarcodeStudyInfo barcodeStudyInfo;
    private final GcpConfig gcpConfig;
    private final BucketContext bucketContext;

    public NotificationService(IbexAdapterService ibexAdapterService, @Qualifier("ibexExecutorService") ExecutorService executorService, BarcodeStudyInfo barcodeStudyInfo, GcpConfig gcpConfig, BucketContext bucketContext) {
        this.ibexAdapterService = ibexAdapterService;
        this.executorService = executorService;
        this.barcodeStudyInfo = barcodeStudyInfo;
        this.gcpConfig = gcpConfig;
        this.bucketContext = bucketContext;
    }

    public ResponseEntity<String> processNotifications(String messagePayload) {
        try {
            if (messagePayload == null || messagePayload.isEmpty()) {
                logger.error("processNotifications :: Message payload is null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Message payload is null or empty");
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(messagePayload);
            String name = root.path("name").asText();
            String mediaLink = root.path("mediaLink").asText();
            if (!name.endsWith(".zip")) {
                logger.info("processNotifications :: Skipping non-ZIP file: {}", name);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file format");
            }
            String studyUid = name.substring(0, name.lastIndexOf("."));
            //Fetch Object metadata
            String bucket = root.path("bucket").asText();
            bucketContext.setBucketName(bucket);
            Storage storage = getStorage();
            if (storage == null) {
                logger.error("processNotifications ::unable to get access to storage");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No storage data is found");
            }
            Blob blob = storage.get(bucket, name);
            if (blob != null && blob.getMetadata() != null) {
                Map<String, String> metadata = blob.getMetadata();
                barcodeStudyInfo.put(metadata.get("barcode"), studyUid);
                executorService.submit(() -> ibexAdapterService.createCaseAndSlide(studyUid, mediaLink, metadata.get("barcode"), metadata.get("seriesid"), metadata.get("dicomstoreurl")));
                return ResponseEntity.ok("Processed");
            } else {
                logger.error("processNotifications :: No metadata found for object: {}", name);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No metadata found for object");
            }

        } catch (JsonMappingException e) {
            logger.error("processNotifications :: Failed to parse JSON: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to parse JSON");
        } catch (Exception e) {
            logger.error("processNotifications :: Unexpected error occurred while processing the notifications: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred");
        }
    }

    private Storage getStorage() {
        try {
            return StorageOptions.newBuilder().setCredentials(ServiceAccountCredentials.fromStream(new ByteArrayInputStream(gcpConfig.getCreds().getBytes(StandardCharsets.UTF_8)))).build().getService();
        } catch (IOException e) {
            logger.error("getStorage :: unable to get access to storage :: {}", e.getMessage());
            return null;
        }
    }

}
