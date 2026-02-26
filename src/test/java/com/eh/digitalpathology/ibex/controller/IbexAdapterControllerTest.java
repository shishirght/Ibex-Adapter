package com.eh.digitalpathology.ibex.controller;

import com.eh.digitalpathology.ibex.client.IbexApiClient;
import com.eh.digitalpathology.ibex.enums.IbexEventTypes;
import com.eh.digitalpathology.ibex.exceptions.IbexApiException;
import com.eh.digitalpathology.ibex.model.BarcodeInstanceRequest;
import com.eh.digitalpathology.ibex.model.Event;
import com.eh.digitalpathology.ibex.service.BarcodeStudyInfo;
import com.eh.digitalpathology.ibex.service.IbexAdapterService;
import com.eh.digitalpathology.ibex.service.KafkaNotificationProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IbexAdapterControllerTest {

    private IbexAdapterService ibexAdapterService;
    private IbexApiClient ibexApiClient;
    private KafkaNotificationProducer kafkaNotificationProducer;
    private BarcodeStudyInfo barcodeStudyInfo;

    private IbexAdapterController controller;

    @BeforeEach
    void setup() {
        ibexAdapterService = mock(IbexAdapterService.class);
        ibexApiClient = mock(IbexApiClient.class);
        kafkaNotificationProducer = mock(KafkaNotificationProducer.class);
        barcodeStudyInfo = mock(BarcodeStudyInfo.class);

        controller = new IbexAdapterController(
                ibexAdapterService,
                ibexApiClient,
                kafkaNotificationProducer,
                barcodeStudyInfo
        );

        ReflectionTestUtils.setField(controller, "emailSvcTopic", "test-topic");
    }

    // =========================
    // getLabel()
    // =========================

    @Test
    void shouldReturnLabelSuccessfully() throws IbexApiException {
        when(ibexApiClient.getLabelingResources()).thenReturn("labels");

        String result = controller.getLabel();

        assertEquals("labels", result);
    }

    @Test
    void shouldWrapIbexApiException() throws IbexApiException {
        when(ibexApiClient.getLabelingResources())
                .thenThrow(new IbexApiException("error"));

        assertThrows(IbexApiException.class, () -> controller.getLabel());
    }

    // =========================
    // handleEvent() - invalid event
    // =========================

    @Test
    void shouldReturnBadRequestForInvalidEvent() throws JsonProcessingException {

        Event event = mock(Event.class);
        when(event.getEventType()).thenReturn("UNKNOWN_EVENT");

        ResponseEntity<String> response = controller.handleEvent(event);

        assertEquals(400, response.getStatusCode().value());
    }

    // =========================
    // SLIDE_DOWNLOAD_COMPLETED
    // =========================

    @Test
    void shouldHandleSlideDownloadCompleted() throws Exception {

        Event event = mock(Event.class);
        when(event.getEventType()).thenReturn(IbexEventTypes.SLIDE_DOWNLOAD_COMPLETED.getEventType());
        when(event.getSubjectId()).thenReturn("BAR123");

        when(barcodeStudyInfo.get("BAR123")).thenReturn("STUDY1");

        ResponseEntity<String> response = controller.handleEvent(event);

        assertEquals(200, response.getStatusCode().value());

        verify(kafkaNotificationProducer).sendNotification(
                eq("test-topic"),
                eq("IBEX_SLIDE_DOWNLOAD_COMPLETED"),
                anyString()
        );

        verify(kafkaNotificationProducer)
                .notifyScanProgressAndUpdateDicomInstances(any(), any());
    }

    // =========================
    // CLASSIFICATION_FINISHED
    // =========================

    @Test
    void shouldHandleClassificationFinished() throws Exception {

        Event event = mock(Event.class);
        when(event.getEventType()).thenReturn(IbexEventTypes.CLASSIFICATION_FINISHED.getEventType());
        when(event.getSubjectId()).thenReturn("BAR123");

        when(barcodeStudyInfo.get("BAR123")).thenReturn("STUDY1");

        ResponseEntity<String> response = controller.handleEvent(event);

        assertEquals(200, response.getStatusCode().value());

        verify(ibexAdapterService).deleteFolderFromBucket("BAR123");
        verify(ibexAdapterService).retrieveFindings("BAR123");
        verify(ibexAdapterService).retrieveHeatmaps("BAR123");
    }

    // =========================
    // CLASSIFICATION_FAILED (error branch)
    // =========================

    @Test
    void shouldHandleClassificationFailed() throws Exception {

        Event event = mock(Event.class);
        when(event.getEventType()).thenReturn(IbexEventTypes.CLASSIFICATION_FAILED.getEventType());
        when(event.getSubjectId()).thenReturn("BAR123");

        when(barcodeStudyInfo.get("BAR123")).thenReturn("");

        ResponseEntity<String> response = controller.handleEvent(event);

        assertEquals(200, response.getStatusCode().value());

        verify(kafkaNotificationProducer)
                .notifyScanProgress(any());
    }

    // =========================
    // REPORT_SUBMITTED
    // =========================

    @Test
    void shouldHandleReportSubmitted() throws Exception {

        Event event = mock(Event.class);
        when(event.getEventType()).thenReturn(IbexEventTypes.REPORT_SUBMITTED.getEventType());
        when(event.getSubjectId()).thenReturn("BAR123");

        when(barcodeStudyInfo.get("BAR123")).thenReturn("STUDY1");

        ResponseEntity<String> response = controller.handleEvent(event);

        assertEquals(200, response.getStatusCode().value());

        verify(kafkaNotificationProducer)
                .sendNotification(eq("test-topic"), eq("IBEX_REPORT_SUBMITTED"), anyString());
    }

}