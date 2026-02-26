package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.client.DBRestClient;
import com.eh.digitalpathology.ibex.model.ApiResponse;
import com.eh.digitalpathology.ibex.model.BarcodeInstanceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    private final DBRestClient dbRestClient;

    public DatabaseService(DBRestClient dbRestClient) {
        this.dbRestClient = dbRestClient;
    }

    public void updateErrorForSlide(String eventType, BarcodeInstanceRequest request) {
        String uri = "dicom/instances/status";
        HttpHeaders headers = setHttpHeaders(eventType);

        logger.info("Updating error status for slide. Barcode: [{}], Series ID: [{}], SOP Instance UID: [{}], Error Message: [{}]",
                request.barcode(), request.seriesId(), request.studyId(), request.errorMessage());
        try {
            dbRestClient.exchange(HttpMethod.PUT, uri, request, new ParameterizedTypeReference<ApiResponse<String>>() {
                    }, httpHeaders -> httpHeaders.putAll(headers))
                    .map(ApiResponse::status)
                    .block();
        } catch (Exception ex) {
            logger.error("Failed to update error message for slide with barcode [{}] and Series ID [{}] SOP Instance UID [{}]. Error message: [{}]. Cause: {}",
                    request.barcode(), request.seriesId(), request.studyId(), request.errorMessage(), ex.getMessage(), ex);
        }
    }

    private static HttpHeaders setHttpHeaders(String serviceName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Service-Name", serviceName);
        return headers;
    }
}
