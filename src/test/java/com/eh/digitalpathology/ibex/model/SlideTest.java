package com.eh.digitalpathology.ibex.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlideTest {

    private Slide slide;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        slide = new Slide();
    }

    // ==========================
    // Constructor Tests
    // ==========================

    @Test
    void testDefaultConstructor_InitializesSlideDetails() {
        assertNotNull(slide.getDetails());
    }

    @Test
    void testDefaultConstructor_DetailsIsSlideDetailsInstance() {
        assertInstanceOf(SlideDetails.class, slide.getDetails());
    }

    @Test
    void testDefaultConstructor_OtherFieldsAreNull() {
        assertNull(slide.getId());
        assertNull(slide.getCaseId());
        assertNull(slide.getSource());
        assertNull(slide.getFileExt());
        assertNull(slide.getScannedDate());
    }

    // ==========================
    // Getter & Setter Tests
    // ==========================

    @Test
    void testSetAndGetId() {
        slide.setId("slide-001");
        assertEquals("slide-001", slide.getId());
    }

    @Test
    void testSetAndGetCaseId() {
        slide.setCaseId("case-999");
        assertEquals("case-999", slide.getCaseId());
    }

    @Test
    void testSetAndGetSource() {
        slide.setSource("GCS");
        assertEquals("GCS", slide.getSource());
    }

    @Test
    void testSetAndGetFileExt() {
        slide.setFileExt("svs");
        assertEquals("svs", slide.getFileExt());
    }

    @Test
    void testSetAndGetScannedDate() {
        slide.setScannedDate("2024-10-10T10:00:00Z");
        assertEquals("2024-10-10T10:00:00Z", slide.getScannedDate());
    }

    @Test
    void testSetId_OverwritesPreviousValue() {
        slide.setId("first-id");
        slide.setId("second-id");
        assertEquals("second-id", slide.getId());
    }

    @Test
    void testSetId_ToNull() {
        slide.setId("slide-001");
        slide.setId(null);
        assertNull(slide.getId());
    }

    @Test
    void testSetCaseId_ToNull() {
        slide.setCaseId("case-001");
        slide.setCaseId(null);
        assertNull(slide.getCaseId());
    }

    @Test
    void testSetSource_ToEmptyString() {
        slide.setSource("");
        assertEquals("", slide.getSource());
    }

    @Test
    void testSetFileExt_ToEmptyString() {
        slide.setFileExt("");
        assertEquals("", slide.getFileExt());
    }

    @Test
    void testSetScannedDate_ToNull() {
        slide.setScannedDate("2024-10-10T00:00:00Z");
        slide.setScannedDate(null);
        assertNull(slide.getScannedDate());
    }

    // ==========================
    // JSON Deserialization Tests
    // ==========================

    @Test
    void testDeserialization_AllFields() throws Exception {
        String json = """
                {
                    "id": "slide-123",
                    "case_id": "case-456",
                    "source": "GCS",
                    "file_extension": "svs",
                    "scanned_at": "2024-10-10T08:00:00Z"
                }
                """;

        Slide deserialized = objectMapper.readValue(json, Slide.class);

        assertEquals("slide-123", deserialized.getId());
        assertEquals("case-456", deserialized.getCaseId());
        assertEquals("GCS", deserialized.getSource());
        assertEquals("svs", deserialized.getFileExt());
        assertEquals("2024-10-10T08:00:00Z", deserialized.getScannedDate());
    }

    @Test
    void testDeserialization_CaseIdSnakeCaseKey() throws Exception {
        String json = "{\"case_id\": \"case-789\"}";
        Slide deserialized = objectMapper.readValue(json, Slide.class);
        assertEquals("case-789", deserialized.getCaseId());
    }

    @Test
    void testDeserialization_FileExtensionSnakeCaseKey() throws Exception {
        String json = "{\"file_extension\": \"tiff\"}";
        Slide deserialized = objectMapper.readValue(json, Slide.class);
        assertEquals("tiff", deserialized.getFileExt());
    }

    @Test
    void testDeserialization_ScannedAtSnakeCaseKey() throws Exception {
        String json = "{\"scanned_at\": \"2024-10-11T09:00:00Z\"}";
        Slide deserialized = objectMapper.readValue(json, Slide.class);
        assertEquals("2024-10-11T09:00:00Z", deserialized.getScannedDate());
    }

    @Test
    void testDeserialization_MissingOptionalFields_DefaultToNull() throws Exception {
        String json = "{\"id\": \"slide-only\"}";
        Slide deserialized = objectMapper.readValue(json, Slide.class);

        assertEquals("slide-only", deserialized.getId());
        assertNull(deserialized.getCaseId());
        assertNull(deserialized.getSource());
        assertNull(deserialized.getFileExt());
        assertNull(deserialized.getScannedDate());
    }

    @Test
    void testDeserialization_EmptyJson_DetailsStillInitialized() throws Exception {
        String json = "{}";
        Slide deserialized = objectMapper.readValue(json, Slide.class);

        assertNotNull(deserialized.getDetails());
    }

    @Test
    void testDeserialization_EmptyJson_AllScalarFieldsNull() throws Exception {
        String json = "{}";
        Slide deserialized = objectMapper.readValue(json, Slide.class);

        assertNull(deserialized.getId());
        assertNull(deserialized.getCaseId());
        assertNull(deserialized.getSource());
        assertNull(deserialized.getFileExt());
        assertNull(deserialized.getScannedDate());
    }

    @Test
    void testDeserialization_NullValues_InJson() throws Exception {
        String json = """
                {
                    "id": null,
                    "case_id": null,
                    "source": null
                }
                """;

        Slide deserialized = objectMapper.readValue(json, Slide.class);

        assertNull(deserialized.getId());
        assertNull(deserialized.getCaseId());
        assertNull(deserialized.getSource());
    }

    // ==========================
    // JSON Serialization Tests
    // ==========================

    @Test
    void testSerialization_CaseIdUsesSnakeCaseKey() throws Exception {
        slide.setCaseId("case-001");
        String json = objectMapper.writeValueAsString(slide);

        assertTrue(json.contains("case_id"));
        assertFalse(json.contains("caseId"));
    }

    @Test
    void testSerialization_FileExtOmitted_WhenNull() throws Exception {
        slide.setFileExt(null);
        String json = objectMapper.writeValueAsString(slide);

        assertFalse(json.contains("file_extension"));
    }

    @Test
    void testSerialization_FileExtOmitted_WhenEmpty() throws Exception {
        slide.setFileExt("");
        String json = objectMapper.writeValueAsString(slide);

        assertFalse(json.contains("file_extension"));
    }

    @Test
    void testSerialization_FileExtIncluded_WhenPresent() throws Exception {
        slide.setFileExt("svs");
        String json = objectMapper.writeValueAsString(slide);

        assertTrue(json.contains("file_extension"));
        assertTrue(json.contains("svs"));
    }

    @Test
    void testSerialization_ScannedAtOmitted_WhenNull() throws Exception {
        slide.setScannedDate(null);
        String json = objectMapper.writeValueAsString(slide);

        assertFalse(json.contains("scanned_at"));
    }

    @Test
    void testSerialization_ScannedAtOmitted_WhenEmpty() throws Exception {
        slide.setScannedDate("");
        String json = objectMapper.writeValueAsString(slide);

        assertFalse(json.contains("scanned_at"));
    }

    @Test
    void testSerialization_ScannedAtIncluded_WhenPresent() throws Exception {
        slide.setScannedDate("2024-10-10T08:00:00Z");
        String json = objectMapper.writeValueAsString(slide);

        assertTrue(json.contains("scanned_at"));
        assertTrue(json.contains("2024-10-10T08:00:00Z"));
    }

    @Test
    void testSerialization_ScannedAtUsesSnakeCaseKey() throws Exception {
        slide.setScannedDate("2024-10-10T08:00:00Z");
        String json = objectMapper.writeValueAsString(slide);

        assertTrue(json.contains("scanned_at"));
        assertFalse(json.contains("scannedDate"));
    }

    @Test
    void testSerialization_FileExtUsesSnakeCaseKey() throws Exception {
        slide.setFileExt("ndpi");
        String json = objectMapper.writeValueAsString(slide);

        assertTrue(json.contains("file_extension"));
        assertFalse(json.contains("fileExt"));
    }

    @Test
    void testSerialization_AllFields() throws Exception {
        slide.setId("slide-999");
        slide.setCaseId("case-888");
        slide.setSource("GCS");
        slide.setFileExt("svs");
        slide.setScannedDate("2024-10-10T00:00:00Z");

        String json = objectMapper.writeValueAsString(slide);

        assertTrue(json.contains("slide-999"));
        assertTrue(json.contains("case-888"));
        assertTrue(json.contains("GCS"));
        assertTrue(json.contains("svs"));
        assertTrue(json.contains("2024-10-10T00:00:00Z"));
    }

    // ==========================
    // Roundtrip Tests
    // ==========================

    @Test
    void testRoundtrip_SerializeAndDeserialize() throws Exception {
        slide.setId("slide-rt");
        slide.setCaseId("case-rt");
        slide.setSource("GCS");
        slide.setFileExt("svs");
        slide.setScannedDate("2024-10-10T06:00:00Z");

        String json = objectMapper.writeValueAsString(slide);
        Slide deserialized = objectMapper.readValue(json, Slide.class);

        assertEquals(slide.getId(), deserialized.getId());
        assertEquals(slide.getCaseId(), deserialized.getCaseId());
        assertEquals(slide.getSource(), deserialized.getSource());
        assertEquals(slide.getFileExt(), deserialized.getFileExt());
        assertEquals(slide.getScannedDate(), deserialized.getScannedDate());
        assertNotNull(deserialized.getDetails());
    }

    @Test
    void testRoundtrip_WithNullOptionalFields() throws Exception {
        slide.setId("slide-no-optional");
        slide.setCaseId("case-no-optional");

        String json = objectMapper.writeValueAsString(slide);
        Slide deserialized = objectMapper.readValue(json, Slide.class);

        assertEquals("slide-no-optional", deserialized.getId());
        assertEquals("case-no-optional", deserialized.getCaseId());
        assertNull(deserialized.getFileExt());
        assertNull(deserialized.getScannedDate());
    }
}