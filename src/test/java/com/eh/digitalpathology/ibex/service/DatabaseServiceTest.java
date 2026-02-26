package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.client.DBRestClient;
import com.eh.digitalpathology.ibex.model.ApiResponse;
import com.eh.digitalpathology.ibex.model.BarcodeInstanceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DatabaseServiceTest {

    private DBRestClient dbRestClient;
    private DatabaseService databaseService;

    @BeforeEach
    void setup() {
        dbRestClient = mock(DBRestClient.class);
        databaseService = new DatabaseService(dbRestClient);
    }

    @Test
    void shouldCallDbRestClientSuccessfully() {

        BarcodeInstanceRequest request = mock(BarcodeInstanceRequest.class);
        when(request.barcode()).thenReturn("B123");
        when(request.seriesId()).thenReturn("S123");
        when(request.studyId()).thenReturn("ST123");
        when(request.errorMessage()).thenReturn("Some error");

        ApiResponse<String> apiResponse = mock(ApiResponse.class);
        when(apiResponse.status()).thenReturn("success");

        when(dbRestClient.exchange(
                any(HttpMethod.class),
                anyString(),
                any(),
                any(ParameterizedTypeReference.class),
                any()
        )).thenReturn(Mono.just(apiResponse));

        databaseService.updateErrorForSlide("TEST_SERVICE", request);

        verify(dbRestClient, times(1)).exchange(
                eq(HttpMethod.PUT),
                eq("dicom/instances/status"),
                eq(request),
                any(ParameterizedTypeReference.class),
                any()
        );
    }

    @Test
    void shouldHandleExceptionGracefully() {

        BarcodeInstanceRequest request = mock(BarcodeInstanceRequest.class);
        when(request.barcode()).thenReturn("B123");
        when(request.seriesId()).thenReturn("S123");
        when(request.studyId()).thenReturn("ST123");
        when(request.errorMessage()).thenReturn("Some error");

        when(dbRestClient.exchange(
                any(),
                anyString(),
                any(),
                any(ParameterizedTypeReference.class),
                any()
        )).thenThrow(new RuntimeException("DB Down"));

        databaseService.updateErrorForSlide("TEST_SERVICE", request);

        verify(dbRestClient, times(1)).exchange(
                eq(HttpMethod.PUT),
                eq("dicom/instances/status"),
                eq(request),
                any(ParameterizedTypeReference.class),
                any()
        );
    }

    @Test
    void shouldSetProperHeader() {

        BarcodeInstanceRequest request = mock(BarcodeInstanceRequest.class);
        when(request.barcode()).thenReturn("B123");
        when(request.seriesId()).thenReturn("S123");
        when(request.studyId()).thenReturn("ST123");
        when(request.errorMessage()).thenReturn("Some error");

        ApiResponse<String> apiResponse = mock(ApiResponse.class);
        when(apiResponse.status()).thenReturn("success");

        ArgumentCaptor<Consumer<HttpHeaders>> headerCaptor =
                ArgumentCaptor.forClass(Consumer.class);

        when(dbRestClient.exchange(
                any(),
                anyString(),
                any(),
                any(ParameterizedTypeReference.class),
                headerCaptor.capture()
        )).thenReturn(Mono.just(apiResponse));

        databaseService.updateErrorForSlide("MY_SERVICE", request);

        HttpHeaders headers = new HttpHeaders();
        headerCaptor.getValue().accept(headers);

        assert headers.getFirst("X-Service-Name").equals("MY_SERVICE");
    }
}