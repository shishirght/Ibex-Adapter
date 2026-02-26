/**
 * IbexAdapterService is responsible for your actual business logic. This service will act on the processed notifications and perform the necessary steps to create a case and a slide. It will also be responsible for interacting with the low-level API wrappers created for the Ibex and Healthcare APIs.
 * Author: Preeti Ankam
 * Date:December 01, 2024
 */

package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.client.HealthcareApiClient;
import com.eh.digitalpathology.ibex.client.IbexApiClient;
import com.eh.digitalpathology.ibex.config.GcpConfig;
import com.eh.digitalpathology.ibex.config.IbexConfig;
import com.eh.digitalpathology.ibex.constants.AppConstants;
import com.eh.digitalpathology.ibex.exceptions.HealthcareApiException;
import com.eh.digitalpathology.ibex.exceptions.IbexApiException;
import com.eh.digitalpathology.ibex.util.AppUtil;
import com.eh.digitalpathology.ibex.util.HttpClientUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@Service
public class IbexAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(IbexAdapterService.class);

    private final HealthcareApiClient healthcareApiClient;
    private final SlideService slideService;
    private final GcpConfig gcpConfig;
    private final IbexConfig ibexConfig;

    private final IbexApiClient ibexApiClient;
    private final BarcodeStudyInfo barcodeStudyInfo;
    private final BucketContext bucketContext;

    public IbexAdapterService(HealthcareApiClient healthcareApiClient, SlideService slideService, GcpConfig gcpConfig, IbexConfig ibexConfig, IbexApiClient ibexApiClient, BarcodeStudyInfo barcodeStudyInfo, BucketContext bucketContext) {
        this.healthcareApiClient = healthcareApiClient;
        this.slideService = slideService;
        this.gcpConfig = gcpConfig;
        this.ibexConfig = ibexConfig;
        this.ibexApiClient = ibexApiClient;
        this.barcodeStudyInfo = barcodeStudyInfo;
        this.bucketContext = bucketContext;
    }

    /**
     * Creates both a case and a slide in the IBEX system using the metadata fetched from the Healthcare API.
     *
     * @param studyUid  The unique study identifier used to fetch metadata.
     * @param sourceUrl The source URL used to create the slide.
     */
    @Async
    public void createCaseAndSlide(String studyUid, String sourceUrl, String barcode, String seriesId, String dicomWebUrl) {

        try {
            // Fetch metadata using Healthcare API
            String metadataJson = healthcareApiClient.fetchMetadata(studyUid, seriesId, dicomWebUrl);
            Map<String, Object> metadataMap = AppUtil.convertStringToJson(metadataJson);
            String seriesUid = AppUtil.getTagValue(metadataMap, AppConstants.SERIES_INSTANCE_UID);

            logger.info("createCaseAndSlide :: Series Uid: {}", seriesUid);
            if (Objects.nonNull(metadataJson)) {
                logger.info("createCaseAndSlide :: Slide creation started for study uid: {} , series uid:{}..", studyUid, seriesUid);
                // Create the slide
                slideService.createSlide(metadataMap, studyUid, seriesUid, sourceUrl, barcode);
            }
        } catch (HealthcareApiException e) {
            logger.error("createCaseAndSlide :: Error fetching metadata from Healthcare API: ", e);
        } catch (IbexApiException e) {
            logger.error("createCaseAndSlide :: Error while creating case and slide: ", e);
        } catch (Exception e) {
            logger.error("createCaseAndSlide :: Exception occurred while creating case and slide: ", e);
        }

    }

    public void retrieveFindings(String slideId) {
        try {
            ibexApiClient.getFindingsOrHeatmaps(slideId, IbexApiClient.FINDINGS_ENDPOINT);
        } catch (IbexApiException e) {
            logger.error("retrieveFindings :: Exception occurred while getting findings for slide id {}: ", slideId, e);
        }
    }

    public void retrieveHeatmaps(String slideId) {
        try {
            String jsonRes = ibexApiClient.getFindingsOrHeatmaps(slideId, IbexApiClient.HEATMAPS_ENDPOINT);
            Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"), slideId);
            Files.createDirectories(tmpDir);
            downloadHeatmaps(jsonRes, tmpDir);
        } catch (IOException | IbexApiException ex) {
            logger.error("retrieveHeatmaps :: Error occurred while retrieving heat maps :: {}", ex.getMessage());
        }

    }

    private void downloadHeatmaps(String jsonRes, Path tmpDir) throws IbexApiException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(jsonRes);

            if (rootNode.isArray()) {
                for (JsonNode heatmapNode : rootNode) {
                    Map<String, Object> heatmapMap = mapper.convertValue(heatmapNode, new TypeReference<>() {
                    });
                    String heatmapUrl = (String) heatmapMap.get("heatmap_url");
                    if (heatmapUrl != null) {
                        saveHeatmap(heatmapUrl, tmpDir);
                    }
                }
            } else {
                logger.warn("downloadHeatmaps :: Expected JSON array for heatmaps, but got: {}", rootNode.getNodeType());
            }
        } catch (IOException e) {
            throw new IbexApiException("downloadHeatmaps :: Error parsing heatmaps JSON: " + e.getMessage());
        }
    }

    private void saveHeatmap(String jsonRes, Path tmpDir) throws IbexApiException {
        Map<String, String> headers = Map.of(AppConstants.API_KEY_HEADER, ibexConfig.getApi().getKey());
        try {
            byte[] response = HttpClientUtil.getImageBytes(jsonRes, headers);
            if (response != null) {
                URI heatmapUri = URI.create(jsonRes);
                String fileName = Paths.get(heatmapUri.getPath()).getFileName().toString();
                Files.write(tmpDir.resolve(fileName), response);
                logger.info("saveHeatmap :: Downloaded and saved heatmap: {}", fileName);
            }
        } catch (HealthcareApiException | IOException e) {
            throw new IbexApiException("saveHeatmap :: Unable to retrieve image and write it into a file :: " + e.getMessage());
        }
    }

    public void deleteFolderFromBucket(String barcode) {
        try {
            String studyUid = barcodeStudyInfo.get(barcode);
            Storage storage = StorageOptions.newBuilder().setCredentials(ServiceAccountCredentials.fromStream(new ByteArrayInputStream(gcpConfig.getCreds().getBytes(StandardCharsets.UTF_8)))).build().getService();

            BlobId blobId = BlobId.of(bucketContext.getBucketName(), studyUid + ".zip");
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                logger.info("deleteFolderFromBucket :: Folder: {}.zip deleted from bucket.", studyUid);
                barcodeStudyInfo.remove(barcode);
            } else {
                logger.info("deleteFolderFromBucket :: Folder: {}.zip not found from bucket.", studyUid);
            }
        } catch (IOException e) {
            logger.info("deleteFolderFromBucket :: IOException occurred while deleting the folder ", e);
        }
    }

}

