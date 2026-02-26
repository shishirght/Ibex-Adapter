package com.eh.digitalpathology.ibex.client;

import com.eh.digitalpathology.ibex.config.IbexConfig;
import com.eh.digitalpathology.ibex.exceptions.IbexApiException;
import com.eh.digitalpathology.ibex.model.HttpResponseWrapper;
import com.eh.digitalpathology.ibex.service.KafkaNotificationProducer;
import com.eh.digitalpathology.ibex.util.HttpClientUtil;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IbexApiClientTest {

    @Mock
    private IbexConfig ibexConfig;

    @Mock
    private IbexConfig.Api api;

    @Mock
    private KafkaNotificationProducer kafkaNotificationProducer;

    @InjectMocks
    private IbexApiClient ibexApiClient;

    @BeforeEach
    void setup() throws Exception {
        when(ibexConfig.getApi()).thenReturn(api);
        when(api.getUrl()).thenReturn("http://test-url");
        when(api.getKey()).thenReturn("test-key");

        // Inject emailSvcTopic manually (because @Value is not processed in unit test)
        Field field = IbexApiClient.class.getDeclaredField("emailSvcTopic");
        field.setAccessible(true);
        field.set(ibexApiClient, "test-topic");
    }

    // =========================
    // CREATE SLIDE
    // =========================

    @Test
    void createSlide_success() {
        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {

            mockedStatic.when(() ->
                            HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_OK, "success"));

            assertDoesNotThrow(() ->
                    ibexApiClient.createSlide("{json}", "barcode1", "series1"));
        }
    }

    @Test
    void createSlide_badRequest() {
        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {

            mockedStatic.when(() ->
                            HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_BAD_REQUEST, "bad"));

            assertThrows(IbexApiException.class, () ->
                    ibexApiClient.createSlide("{json}", "barcode1", "series1"));

            verify(kafkaNotificationProducer, times(1))
                    .sendNotification(anyString(), anyString(), anyString());
        }
    }

    @Test
    void createSlide_conflict() {
        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {

            mockedStatic.when(() ->
                            HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_CONFLICT, "{}"));

            assertThrows(IbexApiException.class, () ->
                    ibexApiClient.createSlide("{json}", "barcode1", "series1"));

            verify(kafkaNotificationProducer, atLeastOnce())
                    .notifyScanProgressAndUpdateDicomInstances(any(), any());
        }
    }

    @Test
    void createSlide_internalServerError() {
        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {

            mockedStatic.when(() ->
                            HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_INTERNAL_SERVER_ERROR, "error"));

            assertThrows(IbexApiException.class, () ->
                    ibexApiClient.createSlide("{json}", "barcode1", "series1"));

            verify(kafkaNotificationProducer, times(1))
                    .sendNotification(anyString(), anyString(), anyString());
        }
    }

    // =========================
    // PUT SUBSCRIPTION
    // =========================

    @Test
    void putSubscription_success() {
        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {

            mockedStatic.when(() ->
                            HttpClientUtil.sendPut(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_OK, "ok"));

            assertDoesNotThrow(() ->
                    ibexApiClient.putSubscription("{sub}"));
        }
    }

    @Test
    void putSubscription_badRequest() {
        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {

            mockedStatic.when(() ->
                            HttpClientUtil.sendPut(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_BAD_REQUEST, "bad"));

            assertThrows(IbexApiException.class, () ->
                    ibexApiClient.putSubscription("{sub}"));
        }
    }

    // =========================
    // LABELING
    // =========================

    @Test
    void getLabelingResources_success() throws IbexApiException {
        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {

            mockedStatic.when(() ->
                            HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_OK, "label-data"));

            String response = ibexApiClient.getLabelingResources();

            assertEquals("label-data", response);
        }
    }

    @Test
    void getLabelingResources_unauthorized() {
        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {

            mockedStatic.when(() ->
                            HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_UNAUTHORIZED, "unauth"));

            assertThrows(IbexApiException.class,
                    () -> ibexApiClient.getLabelingResources());
        }
    }

    // =========================
    // FINDINGS / HEATMAPS
    // =========================

    @Test
    void getFindingsOrHeatmaps_success_withSlideId() {
        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {

            mockedStatic.when(() ->
                            HttpClientUtil.sendGet(contains("slide=123"), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_OK, "findings"));

            String response = ibexApiClient.getFindingsOrHeatmaps("123", "/findings");

            assertEquals("findings", response);
        } catch (IbexApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getFindingsOrHeatmaps_internalServerError() {
        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {

            mockedStatic.when(() ->
                            HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_INTERNAL_SERVER_ERROR, "error"));

            assertThrows(IbexApiException.class,
                    () -> ibexApiClient.getFindingsOrHeatmaps("123", "/heatmaps"));
        }
    }
}