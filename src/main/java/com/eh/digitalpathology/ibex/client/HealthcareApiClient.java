/**
 * HealthcareApiClient class acts as a client for the DICOM Healthcare API, encapsulating
 * the logic for API calls to DICOM stores. It provides a reusable and
 * modular approach for accessing healthcare DICOM data while managing
 * errors through custom exceptions.
 * Author: Preeti Ankam
 * Date: December 23, 2024
 */

package com.eh.digitalpathology.ibex.client;

import com.eh.digitalpathology.ibex.config.GcpConfig;
import com.eh.digitalpathology.ibex.exceptions.HealthcareApiException;
import com.eh.digitalpathology.ibex.util.GCPUtils;
import com.eh.digitalpathology.ibex.util.HttpClientUtil;
import com.eh.digitalpathology.ibex.model.HttpResponseWrapper;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HealthcareApiClient {

    private final Logger log = LoggerFactory.getLogger(HealthcareApiClient.class.getName());
    private final GcpConfig gcpConfig;

    public HealthcareApiClient(GcpConfig gcpConfig) {
        this.gcpConfig = gcpConfig;
    }

    /**
     * Fetches metadata from the Healthcare API using the provided studyUid.
     *
     * @param studyUid The unique study identifier.
     * @return Metadata as a JSON string.
     * @throws HealthcareApiException If any error occurs during the API call.
     */
    public String fetchMetadata(String studyUid, String seriesId,String dicomWebUrl) throws HealthcareApiException {
        try {
            log.info("fetchMetadata :: seriesId: {}", seriesId);
            log.info("fetchMetadata :: dicomWebUrl: {}", dicomWebUrl);
            String uri = String.format("%s/%s/dicomWeb/studies/%s/series/%s/metadata", this.gcpConfig.getDicomWebUrl(), dicomWebUrl, studyUid, seriesId);

            log.info("fetchMetadata :: Constructed DICOM Store URI: {}", uri);

            String accessToken = GCPUtils.getAccessToken(gcpConfig);
            log.info("Access token retrieved successfully.");

            // Prepare headers
            Map<String, String> headers = new HashMap<>();
            if (accessToken != null && !accessToken.isEmpty()) {
                headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            }
            headers.put(HttpHeaders.ACCEPT, "application/dicom+json");

            // Send GET request using utility
            HttpResponseWrapper response = HttpClientUtil.sendGet(uri, headers);
            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode == HttpStatus.SC_OK) {
                return responseBody;
            } else {
                log.error("Failed to fetch metadata. Status: {}, Body: {}", statusCode, responseBody);
                throw new HealthcareApiException("Failed to fetch metadata. Status: " + statusCode + " :: " + responseBody);
            }

        } catch (Exception e) {
            throw new HealthcareApiException("Error occurred while fetching metadata from the Healthcare API.", e);
        }
    }
}
