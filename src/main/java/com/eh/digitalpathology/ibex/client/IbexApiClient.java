/**
 * This class is responsible for interacting with the IBEX API.
 * It provides methods to send requests to various IBEX endpoints, such as:
 * - Creating cases
 * - Fetching labeling resources
 * <p>
 * In case of any errors during API interactions (e.g., network failures, invalid responses),
 * an IbexApiException is thrown to indicate the failure.
 * Author: Preeti Ankam
 * Date: December 23, 2024
 */

package com.eh.digitalpathology.ibex.client;

import com.eh.digitalpathology.ibex.config.IbexConfig;
import com.eh.digitalpathology.ibex.constants.AppConstants;
import com.eh.digitalpathology.ibex.exceptions.IbexApiException;
import com.eh.digitalpathology.ibex.model.BarcodeInstanceRequest;
import com.eh.digitalpathology.ibex.model.HttpResponseWrapper;
import com.eh.digitalpathology.ibex.model.SlideErrorInfo;
import com.eh.digitalpathology.ibex.model.SlideScanProgressEvent;
import com.eh.digitalpathology.ibex.service.KafkaNotificationProducer;
import com.eh.digitalpathology.ibex.util.HttpClientUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

import static com.eh.digitalpathology.ibex.constants.AppConstants.BAD_REQUEST;

@RefreshScope
@Component
public class IbexApiClient {


    private final IbexConfig ibexConfig;
    @Value("${kafka.topic.email}")
    private String emailSvcTopic;

    // Constants for IBEX  specific endpoints
    private static final String SLIDES_ENDPOINT = "/slides";
    private static final String LABELING_ENDPOINT = "/labeling";
    private static final String SUBSCRIPTION_ENDPOINT = "/subscription";
    public static final String FINDINGS_ENDPOINT = "/findings";
    public static final String HEATMAPS_ENDPOINT = "/heatmaps";
    private final KafkaNotificationProducer kafkaNotificationProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(IbexApiClient.class);

    public IbexApiClient(IbexConfig ibexConfig, KafkaNotificationProducer kafkaNotificationProducer) {
        this.ibexConfig = ibexConfig;
        this.kafkaNotificationProducer = kafkaNotificationProducer;
    }

    public void createSlide(String slideData, String barcode,String seriesUid) throws IbexApiException {
        String url = ibexConfig.getApi().getUrl() + SLIDES_ENDPOINT;
        Map<String, String> headers = Map.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, AppConstants.API_KEY_HEADER, ibexConfig.getApi().getKey());
        logger.info("createSlide :: Sending slide creation request to URL: {} with payload: {}", url, slideData);

        try {
            HttpResponseWrapper response = HttpClientUtil.sendPost(url, slideData, headers);
            int statusCode = response.statusCode();
            String responseBody = response.body();

            switch (statusCode) {
                case HttpStatus.SC_OK, HttpStatus.SC_CREATED, HttpStatus.SC_ACCEPTED ->
                        logger.info("Slide creation successful. Response: {}", responseBody);
                case HttpStatus.SC_BAD_REQUEST -> {
                    SlideErrorInfo errorInfo = new SlideErrorInfo(barcode, HttpStatus.SC_BAD_REQUEST, "BAD REQUEST");
                    kafkaNotificationProducer.sendNotification(emailSvcTopic, AppConstants.IBEX_SLIDE_CREATION_ERROR, objectMapper.writeValueAsString(errorInfo));
                    throw new IbexApiException(BAD_REQUEST + " :: " + responseBody);
                }
                case HttpStatus.SC_UNAUTHORIZED -> {
                    SlideErrorInfo errorInfo = new SlideErrorInfo(barcode, HttpStatus.SC_UNAUTHORIZED, "UNAUTHORIZED");
                    kafkaNotificationProducer.sendNotification(emailSvcTopic, AppConstants.IBEX_SLIDE_CREATION_ERROR, objectMapper.writeValueAsString(errorInfo));
                    throw new IbexApiException(AppConstants.UNAUTHORIZED_ERROR);
                }
                case HttpStatus.SC_FORBIDDEN -> {
                    SlideErrorInfo errorInfo = new SlideErrorInfo(barcode, HttpStatus.SC_FORBIDDEN, "FORBIDDEN");
                    kafkaNotificationProducer.sendNotification(emailSvcTopic, AppConstants.IBEX_SLIDE_CREATION_ERROR, objectMapper.writeValueAsString(errorInfo));
                    throw new IbexApiException("403 FORBIDDEN: " + responseBody);
                }
                case HttpStatus.SC_CONFLICT -> {
                    SlideErrorInfo errorInfo = new SlideErrorInfo(barcode, HttpStatus.SC_CONFLICT, "CONFLICT");
                    kafkaNotificationProducer.sendNotification(emailSvcTopic, AppConstants.IBEX_SLIDE_CREATION_ERROR, objectMapper.writeValueAsString(errorInfo));
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    SlideScanProgressEvent slideScanProgressEvent = new SlideScanProgressEvent(barcode,"ibex-slide-creation-failed");
                    BarcodeInstanceRequest barcodeInstanceRequest = new BarcodeInstanceRequest(barcode,null,seriesUid,"Slide "+barcode+" already exists, slide names must be unique.");
                    kafkaNotificationProducer.notifyScanProgressAndUpdateDicomInstances(slideScanProgressEvent,barcodeInstanceRequest);
                    throw new IbexApiException("409 CONFLICT: " + responseBody);
                }
                case HttpStatus.SC_INTERNAL_SERVER_ERROR -> {
                    SlideErrorInfo errorInfo = new SlideErrorInfo(barcode, HttpStatus.SC_INTERNAL_SERVER_ERROR, "INTERNAL SERVER ERROR");
                    kafkaNotificationProducer.sendNotification(emailSvcTopic, AppConstants.IBEX_SLIDE_CREATION_ERROR, objectMapper.writeValueAsString(errorInfo));
                    throw new IbexApiException(AppConstants.INTERNAL_SERVER_ERROR);
                }
                default -> {
                    SlideErrorInfo errorInfo = new SlideErrorInfo(barcode, statusCode, "INTERNAL SERVER ERROR");
                    kafkaNotificationProducer.sendNotification(emailSvcTopic, AppConstants.IBEX_SLIDE_CREATION_ERROR, objectMapper.writeValueAsString(errorInfo));
                    throw new IbexApiException(AppConstants.STATUS_CODE + statusCode + " :: " + responseBody);
                }
            }

        } catch (Exception e) {
            throw new IbexApiException(e);
        }

    }

    public void putSubscription(String subscriptionData) throws IbexApiException {
        String url = ibexConfig.getApi().getUrl() + SUBSCRIPTION_ENDPOINT;
        Map<String, String> headers = Map.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, AppConstants.API_KEY_HEADER, ibexConfig.getApi().getKey());
        try {
            HttpResponseWrapper response = HttpClientUtil.sendPut(url, subscriptionData, headers);
            int statusCode = response.statusCode();
            String responseBody = response.body();
            switch (statusCode) {
                case HttpStatus.SC_OK -> logger.info("Updated Subscription. Response: {}", responseBody);
                case HttpStatus.SC_BAD_REQUEST -> throw new IbexApiException(BAD_REQUEST + " :: " + responseBody);
                case HttpStatus.SC_UNAUTHORIZED -> throw new IbexApiException(AppConstants.UNAUTHORIZED_ERROR);
                case HttpStatus.SC_INTERNAL_SERVER_ERROR ->
                        throw new IbexApiException(AppConstants.INTERNAL_SERVER_ERROR);
                default -> throw new IbexApiException(AppConstants.STATUS_CODE + statusCode);
            }
        } catch (Exception e) {
            throw new IbexApiException(e);
        }
    }

    public String getLabelingResources() throws IbexApiException {
        String url = ibexConfig.getApi().getUrl() + LABELING_ENDPOINT;
        Map<String, String> headers = Map.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, AppConstants.API_KEY_HEADER, ibexConfig.getApi().getKey());
        try {
            HttpResponseWrapper response = HttpClientUtil.sendGet(url, headers);
            int statusCode = response.statusCode();
            String responseBody = response.body();
            switch (statusCode) {
                case HttpStatus.SC_OK, HttpStatus.SC_CREATED, HttpStatus.SC_ACCEPTED -> {
                    logger.info("getLabelingResources :: Response: {}", responseBody);
                    return responseBody;
                }
                case HttpStatus.SC_BAD_REQUEST -> throw new IbexApiException(BAD_REQUEST + " :: " + responseBody);
                case HttpStatus.SC_UNAUTHORIZED -> throw new IbexApiException(AppConstants.UNAUTHORIZED_ERROR);
                case HttpStatus.SC_FORBIDDEN -> throw new IbexApiException("403 FORBIDDEN: " + responseBody);
                case HttpStatus.SC_CONFLICT -> throw new IbexApiException("409 CONFLICT: " + responseBody);
                case HttpStatus.SC_INTERNAL_SERVER_ERROR ->
                        throw new IbexApiException(AppConstants.INTERNAL_SERVER_ERROR);
                default -> throw new IbexApiException(AppConstants.STATUS_CODE + statusCode + " :: " + responseBody);
            }
        } catch (Exception e) {
            throw new IbexApiException(e);
        }
    }

    public String getFindingsOrHeatmaps(String slideId, String endpoint) throws IbexApiException {
        String findingsUrl = ibexConfig.getApi().getUrl() + endpoint;
        URI uri = URI.create(findingsUrl);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        if (slideId != null && !slideId.isEmpty()) {
            builder.queryParam("slide", slideId);
        }
        Map<String, String> headers = Map.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, AppConstants.API_KEY_HEADER, ibexConfig.getApi().getKey());
        try {
            HttpResponseWrapper response = HttpClientUtil.sendGet(builder.build().toString(), headers);
            int statusCode = response.statusCode();
            String responseBody = response.body();
            switch (statusCode) {
                case HttpStatus.SC_OK -> {
                    logger.info("getFindingsOrHeatmaps :: Response: {}", responseBody);
                    return responseBody;
                }
                case HttpStatus.SC_BAD_REQUEST -> throw new IbexApiException(BAD_REQUEST + " :: " + responseBody);
                case HttpStatus.SC_UNAUTHORIZED -> throw new IbexApiException(AppConstants.UNAUTHORIZED_ERROR);
                case HttpStatus.SC_INTERNAL_SERVER_ERROR ->
                        throw new IbexApiException(AppConstants.INTERNAL_SERVER_ERROR);
                default -> throw new IbexApiException(AppConstants.STATUS_CODE + statusCode);
            }
        } catch (Exception ex) {
            throw new IbexApiException(ex);
        }
    }
}