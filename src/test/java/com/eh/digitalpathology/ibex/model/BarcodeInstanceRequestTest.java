package com.eh.digitalpathology.ibex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeInstanceRequestTest {

    @Test
    void constructor_andAccessors_workCorrectly() {
        BarcodeInstanceRequest request = new BarcodeInstanceRequest("BC001", "study1", "series1", "error msg");

        assertEquals("BC001", request.barcode());
        assertEquals("study1", request.studyId());
        assertEquals("series1", request.seriesId());
        assertEquals("error msg", request.errorMessage());
    }

    @Test
    void constructor_withNullValues_allowsNulls() {
        BarcodeInstanceRequest request = new BarcodeInstanceRequest(null, null, null, null);

        assertNull(request.barcode());
        assertNull(request.studyId());
        assertNull(request.seriesId());
        assertNull(request.errorMessage());
    }

    @Test
    void jsonIgnoreProperties_annotationIsPresent() {
        JsonIgnoreProperties annotation = BarcodeInstanceRequest.class.getAnnotation(JsonIgnoreProperties.class);

        assertNotNull(annotation);
        assertTrue(annotation.ignoreUnknown());
    }

    @Test
    void deserialization_ignoresUnknownFields() throws Exception {
        String json = """
                {
                    "barcode": "BC001",
                    "studyId": "study1",
                    "seriesId": "series1",
                    "errorMessage": "error msg",
                    "unknownField": "should be ignored"
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper();
        BarcodeInstanceRequest request = objectMapper.readValue(json, BarcodeInstanceRequest.class);

        assertEquals("BC001", request.barcode());
        assertEquals("study1", request.studyId());
        assertEquals("series1", request.seriesId());
        assertEquals("error msg", request.errorMessage());
    }
}