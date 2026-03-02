package com.eh.digitalpathology.ibex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlideScanProgressEventTest {

    @Test
    void canonicalConstructor_allFieldsSet() {
        SlideScanProgressEvent event = new SlideScanProgressEvent("BC001", "COMPLETED", "none", "custom-service");

        assertEquals("BC001", event.slideBarcode());
        assertEquals("COMPLETED", event.scanStatus());
        assertEquals("none", event.errorMessage());
        assertEquals("custom-service", event.sourceService());
    }

    @Test
    void threeArgConstructor_setsDefaultSourceService() {
        SlideScanProgressEvent event = new SlideScanProgressEvent("BC002", "FAILED", "some error");

        assertEquals("BC002", event.slideBarcode());
        assertEquals("FAILED", event.scanStatus());
        assertEquals("some error", event.errorMessage());
        assertEquals("eh-ibex-adapter", event.sourceService());
    }

    @Test
    void twoArgConstructor_setsNullErrorMessageAndDefaultSourceService() {
        SlideScanProgressEvent event = new SlideScanProgressEvent("BC003", "IN_PROGRESS");

        assertEquals("BC003", event.slideBarcode());
        assertEquals("IN_PROGRESS", event.scanStatus());
        assertNull(event.errorMessage());
        assertEquals("eh-ibex-adapter", event.sourceService());
    }

    @Test
    void jsonIgnoreProperties_annotationIsPresent() {
        JsonIgnoreProperties annotation = SlideScanProgressEvent.class.getAnnotation(JsonIgnoreProperties.class);
        assertNotNull(annotation);
        assertTrue(annotation.ignoreUnknown());
    }

    @Test
    void deserialization_ignoresUnknownFields() throws Exception {
        String json = """
                {
                    "slideBarcode": "BC001",
                    "scanStatus": "COMPLETED",
                    "errorMessage": "none",
                    "sourceService": "custom-service",
                    "unknownField": "should be ignored"
                }
                """;

        SlideScanProgressEvent event = new ObjectMapper().readValue(json, SlideScanProgressEvent.class);

        assertEquals("BC001", event.slideBarcode());
        assertEquals("COMPLETED", event.scanStatus());
        assertEquals("none", event.errorMessage());
        assertEquals("custom-service", event.sourceService());
    }
}