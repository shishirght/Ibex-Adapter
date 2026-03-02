package com.eh.digitalpathology.ibex.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CaseStudyTest {

    @Test
    void setAndGetBarcode_returnsCorrectValue() {
        CaseStudy caseStudy = new CaseStudy();
        caseStudy.setBarcode("BC001");
        assertEquals("BC001", caseStudy.getBarcode());
    }

    @Test
    void defaultBarcode_isNull() {
        CaseStudy caseStudy = new CaseStudy();
        assertNull(caseStudy.getBarcode());
    }

    @Test
    void jsonInclude_annotationIsPresent_andNonNull() {
        JsonInclude annotation = CaseStudy.class.getAnnotation(JsonInclude.class);
        assertNotNull(annotation);
        assertEquals(JsonInclude.Include.NON_NULL, annotation.value());
    }

    @Test
    void serialization_excludesNullFields() throws Exception {
        CaseStudy caseStudy = new CaseStudy();
        String json = new ObjectMapper().writeValueAsString(caseStudy);
        assertFalse(json.contains("barcode"));
    }

    @Test
    void serialization_includesNonNullFields() throws Exception {
        CaseStudy caseStudy = new CaseStudy();
        caseStudy.setBarcode("BC001");
        String json = new ObjectMapper().writeValueAsString(caseStudy);
        assertTrue(json.contains("BC001"));
    }
}