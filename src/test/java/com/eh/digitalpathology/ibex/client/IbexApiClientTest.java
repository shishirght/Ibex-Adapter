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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

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

        Field field = IbexApiClient.class.getDeclaredField("emailSvcTopic");
        field.setAccessible(true);
        field.set(ibexApiClient, "test-topic");
    }


    @Test
    void createSlide_200_ok() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_OK, "success"));

            assertDoesNotThrow(() -> ibexApiClient.createSlide("{}", "b1", "s1"));
        }
    }

    @Test
    void createSlide_201_created() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_CREATED, "created"));

            assertDoesNotThrow(() -> ibexApiClient.createSlide("{}", "b1", "s1"));
        }
    }

    @Test
    void createSlide_202_accepted() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_ACCEPTED, "accepted"));

            assertDoesNotThrow(() -> ibexApiClient.createSlide("{}", "b1", "s1"));
        }
    }

    @Test
    void createSlide_400_badRequest() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_BAD_REQUEST, "bad"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.createSlide("{}", "b1", "s1"));
            verify(kafkaNotificationProducer).sendNotification(anyString(), anyString(), anyString());
        }
    }

    @Test
    void createSlide_401_unauthorized() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_UNAUTHORIZED, "unauth"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.createSlide("{}", "b1", "s1"));
            verify(kafkaNotificationProducer).sendNotification(anyString(), anyString(), anyString());
        }
    }

    @Test
    void createSlide_403_forbidden() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_FORBIDDEN, "forbidden"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.createSlide("{}", "b1", "s1"));
            verify(kafkaNotificationProducer).sendNotification(anyString(), anyString(), anyString());
        }
    }

    @Test
    void createSlide_409_conflict() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_CONFLICT, "{}"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.createSlide("{}", "b1", "s1"));
            verify(kafkaNotificationProducer).sendNotification(anyString(), anyString(), anyString());
            verify(kafkaNotificationProducer).notifyScanProgressAndUpdateDicomInstances(any(), any());
        }
    }

    @Test
    void createSlide_500_internalServerError() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_INTERNAL_SERVER_ERROR, "error"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.createSlide("{}", "b1", "s1"));
            verify(kafkaNotificationProducer).sendNotification(anyString(), anyString(), anyString());
        }
    }

    @Test
    void createSlide_default_unknownStatus() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(418, "teapot"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.createSlide("{}", "b1", "s1"));
            verify(kafkaNotificationProducer).sendNotification(anyString(), anyString(), anyString());
        }
    }

    @Test
    void createSlide_httpClientThrows_wrapsInIbexApiException() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPost(anyString(), anyString(), anyMap()))
                    .thenThrow(new RuntimeException("network error"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.createSlide("{}", "b1", "s1"));
        }
    }


    @Test
    void putSubscription_200_ok() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPut(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_OK, "ok"));

            assertDoesNotThrow(() -> ibexApiClient.putSubscription("{sub}"));
        }
    }

    @Test
    void putSubscription_400_badRequest() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPut(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_BAD_REQUEST, "bad"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.putSubscription("{sub}"));
        }
    }

    @Test
    void putSubscription_401_unauthorized() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPut(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_UNAUTHORIZED, "unauth"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.putSubscription("{sub}"));
        }
    }

    @Test
    void putSubscription_500_internalServerError() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPut(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_INTERNAL_SERVER_ERROR, "err"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.putSubscription("{sub}"));
        }
    }

    @Test
    void putSubscription_default_unknownStatus() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPut(anyString(), anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(418, "teapot"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.putSubscription("{sub}"));
        }
    }

    @Test
    void putSubscription_httpClientThrows_wrapsInIbexApiException() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendPut(anyString(), anyString(), anyMap()))
                    .thenThrow(new RuntimeException("network error"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.putSubscription("{sub}"));
        }
    }

    @Test
    void getLabelingResources_200_ok() throws IbexApiException {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_OK, "label-data"));

            assertEquals("label-data", ibexApiClient.getLabelingResources());
        }
    }

    @Test
    void getLabelingResources_201_created() throws IbexApiException {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_CREATED, "created-data"));

            assertEquals("created-data", ibexApiClient.getLabelingResources());
        }
    }

    @Test
    void getLabelingResources_202_accepted() throws IbexApiException {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_ACCEPTED, "accepted-data"));

            assertEquals("accepted-data", ibexApiClient.getLabelingResources());
        }
    }

    @Test
    void getLabelingResources_400_badRequest() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_BAD_REQUEST, "bad"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.getLabelingResources());
        }
    }

    @Test
    void getLabelingResources_401_unauthorized() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_UNAUTHORIZED, "unauth"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.getLabelingResources());
        }
    }

    @Test
    void getLabelingResources_403_forbidden() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_FORBIDDEN, "forbidden"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.getLabelingResources());
        }
    }

    @Test
    void getLabelingResources_409_conflict() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_CONFLICT, "conflict"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.getLabelingResources());
        }
    }

    @Test
    void getLabelingResources_500_internalServerError() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_INTERNAL_SERVER_ERROR, "err"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.getLabelingResources());
        }
    }

    @Test
    void getLabelingResources_default_unknownStatus() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(418, "teapot"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.getLabelingResources());
        }
    }

    @Test
    void getLabelingResources_httpClientThrows_wrapsInIbexApiException() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenThrow(new RuntimeException("network error"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.getLabelingResources());
        }
    }

    @Test
    void getFindingsOrHeatmaps_200_withSlideId() throws IbexApiException {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(contains("slide=s1"), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_OK, "findings-data"));

            assertEquals("findings-data", ibexApiClient.getFindingsOrHeatmaps("s1", "/findings"));
        }
    }

    @Test
    void getFindingsOrHeatmaps_200_nullSlideId_skipsQueryParam() throws IbexApiException {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_OK, "heatmap-data"));

            assertEquals("heatmap-data", ibexApiClient.getFindingsOrHeatmaps(null, "/heatmaps"));
        }
    }

    @Test
    void getFindingsOrHeatmaps_200_emptySlideId_skipsQueryParam() throws IbexApiException {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_OK, "heatmap-data"));

            assertEquals("heatmap-data", ibexApiClient.getFindingsOrHeatmaps("", "/heatmaps"));
        }
    }

    @Test
    void getFindingsOrHeatmaps_400_badRequest() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_BAD_REQUEST, "bad"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.getFindingsOrHeatmaps("s1", "/findings"));
        }
    }

    @Test
    void getFindingsOrHeatmaps_401_unauthorized() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_UNAUTHORIZED, "unauth"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.getFindingsOrHeatmaps("s1", "/findings"));
        }
    }

    @Test
    void getFindingsOrHeatmaps_500_internalServerError() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(HttpStatus.SC_INTERNAL_SERVER_ERROR, "err"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.getFindingsOrHeatmaps("s1", "/heatmaps"));
        }
    }

    @Test
    void getFindingsOrHeatmaps_default_unknownStatus() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenReturn(new HttpResponseWrapper(418, "teapot"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.getFindingsOrHeatmaps("s1", "/findings"));
        }
    }

    @Test
    void getFindingsOrHeatmaps_httpClientThrows_wrapsInIbexApiException() {
        try (MockedStatic<HttpClientUtil> mock = mockStatic(HttpClientUtil.class)) {
            mock.when(() -> HttpClientUtil.sendGet(anyString(), anyMap()))
                    .thenThrow(new RuntimeException("network error"));

            assertThrows(IbexApiException.class, () -> ibexApiClient.getFindingsOrHeatmaps("s1", "/findings"));
        }
    }
}