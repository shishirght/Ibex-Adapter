package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.client.IbexApiClient;
import com.eh.digitalpathology.ibex.config.OrganMappingConfig;
import com.eh.digitalpathology.ibex.constants.AppConstants;
import com.eh.digitalpathology.ibex.exceptions.IbexApiException;
import com.eh.digitalpathology.ibex.exceptions.SignedUrlGenerationException;
import com.eh.digitalpathology.ibex.model.SlideScanProgressEvent;
import com.eh.digitalpathology.ibex.util.AppUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlideServiceTest {

    @Mock
    private SignedUrlCreationService signedUrlCreationService;

    @Mock
    private IbexApiClient ibexApiClient;

    @Mock
    private OrganMappingConfig organMappingConfig;

    @Mock
    private KafkaNotificationProducer kafkaNotificationProducer;

    @Mock
    private ApiSignedUrlService apiSignedUrlService;

    @InjectMocks
    private SlideService slideService;

    private Map<String, Object> metadata;

    @BeforeEach
    void setup() throws Exception {
        metadata = new HashMap<>();

        when(organMappingConfig.getAllowed()).thenReturn(List.of("lung"));
        when(organMappingConfig.getMapping()).thenReturn(Map.of("lung_code", "lung"));

        // inject networkEnabled flag manually
        Field field = SlideService.class.getDeclaredField("networkEnabled");
        field.setAccessible(true);
        field.set(slideService, true);
    }

    @Test
    void createSlide_success_networkEnabled() throws Exception {

        try (MockedStatic<AppUtil> mockedStatic = mockStatic(AppUtil.class)) {

            mockedStatic.when(() ->
                            AppUtil.getTagValue(any(), anyString()))
                    .thenReturn("CASE123");

            mockedStatic.when(() ->
                            AppUtil.extractOrganType(any(), anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(Map.of("key", "lung_code"));

            mockedStatic.when(() -> AppUtil.validate(anyString()))
                    .thenReturn(true);

            mockedStatic.when(() -> AppUtil.getStainValue(any()))
                    .thenReturn(Optional.empty());

            mockedStatic.when(() -> AppUtil.convertObjectToString(any()))
                    .thenReturn("{json}");

            when(signedUrlCreationService.generateSignedUrl(anyString()))
                    .thenReturn("signed-url");

            assertDoesNotThrow(() ->
                    slideService.createSlide(metadata, "study1", "series1", "sourceUrl", "barcode1"));

            verify(ibexApiClient, times(1))
                    .createSlide(eq("{json}"), eq("barcode1"), eq("series1"));
        }
    }

    @Test
    void createSlide_unsupportedOrgan() throws IbexApiException {

        try (MockedStatic<AppUtil> mockedStatic = mockStatic(AppUtil.class)) {

            mockedStatic.when(() ->
                            AppUtil.getTagValue(any(), anyString()))
                    .thenReturn("CASE123");

            mockedStatic.when(() ->
                            AppUtil.extractOrganType(any(), anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(Map.of("key", "brain"));

            when(organMappingConfig.getAllowed()).thenReturn(List.of("lung"));

            slideService.createSlide(metadata, "study1", "series1", "url", "barcode1");

            verify(kafkaNotificationProducer, times(1))
                    .notifyScanProgress(any(SlideScanProgressEvent.class));

            verifyNoInteractions(ibexApiClient);
        }
    }

    @Test
    void createSlide_invalidBarcodeOrCaseId() {

        try (MockedStatic<AppUtil> mockedStatic = mockStatic(AppUtil.class)) {

            mockedStatic.when(() ->
                            AppUtil.getTagValue(any(), anyString()))
                    .thenReturn("CASE123");

            mockedStatic.when(() ->
                            AppUtil.extractOrganType(any(), anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(Map.of("key", "lung_code"));

            mockedStatic.when(() -> AppUtil.validate(anyString()))
                    .thenReturn(false);

            assertThrows(IbexApiException.class, () ->
                    slideService.createSlide(metadata, "study1", "series1", "url", "barcode1"));

            verify(kafkaNotificationProducer, times(1))
                    .notifyScanProgressAndUpdateDicomInstances(any(), any());
        }
    }

    @Test
    @Disabled
    void createSlide_signedUrlIOException() throws Exception {

        try (MockedStatic<AppUtil> mockedStatic = mockStatic(AppUtil.class)) {

            mockedStatic.when(() ->
                            AppUtil.getTagValue(any(), anyString()))
                    .thenReturn("CASE123");

            mockedStatic.when(() ->
                            AppUtil.extractOrganType(any(), anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(Map.of("key", "lung_code"));

            mockedStatic.when(() -> AppUtil.validate(anyString()))
                    .thenReturn(true);

            mockedStatic.when(() -> AppUtil.getStainValue(any()))
                    .thenReturn(Optional.empty());

            mockedStatic.when(() -> AppUtil.convertObjectToString(any()))
                    .thenReturn("{json}");

            when(signedUrlCreationService.generateSignedUrl(anyString()))
                    .thenThrow(new IOException("error"));

            assertThrows(SignedUrlGenerationException.class, () ->
                    slideService.createSlide(metadata, "study1", "series1", "url", "barcode1"));
        }
    }

//    @Test
//    void getApiSignedUrl_success() throws Exception {
//
//        Field field = SlideService.class.getDeclaredField("networkEnabled");
//        field.setAccessible(true);
//        field.set(slideService, false);
//
//        when(apiSignedUrlService.generateApiSignedUrl(anyString(), anyString(), any()))
//                .thenReturn("api-signed-url");
//
//        String url = "https://storage.googleapis.com/b/test-bucket/o/test-object?generation=123";
//
//        String result = slideService.createApiSignedUrlForTest(url);
//
//        assertEquals("api-signed-url", result);
//    }

    @Test
    @Disabled
    void resolveOrgan_returnsMappedValue() {
        String result = slideService.resolveOrgan("lung_code");
        assertEquals("lung", result);
    }
}