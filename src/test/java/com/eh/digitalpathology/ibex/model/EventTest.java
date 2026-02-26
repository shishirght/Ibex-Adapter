package com.eh.digitalpathology.ibex.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    private Event event;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        event = new Event();
    }

    // ==========================
    // Getter & Setter Tests
    // ==========================

    @Test
    void testSetAndGetId() {
        event.setId("event-001");
        assertEquals("event-001", event.getId());
    }

    @Test
    void testSetAndGetEventType() {
        event.setEventType("CLASSIFICATION_FINISHED");
        assertEquals("CLASSIFICATION_FINISHED", event.getEventType());
    }

    @Test
    void testSetAndGetPriority() {
        event.setPriority("HIGH");
        assertEquals("HIGH", event.getPriority());
    }

    @Test
    void testSetAndGetSubjectId() {
        event.setSubjectId("slide-123");
        assertEquals("slide-123", event.getSubjectId());
    }

    @Test
    void testSetAndGetSubjectType() {
        event.setSubjectType("SLIDE");
        assertEquals("SLIDE", event.getSubjectType());
    }

    @Test
    void testSetAndGetSubjectUrl() {
        event.setSubjectUrl("https://example.com/slides/123");
        assertEquals("https://example.com/slides/123", event.getSubjectUrl());
    }

    @Test
    void testSetAndGetDescription() {
        event.setDescription("Slide classification completed.");
        assertEquals("Slide classification completed.", event.getDescription());
    }

    @Test
    void testSetAndGetCreatedAt() {
        event.setCreatedAt("2024-11-30T10:15:30Z");
        assertEquals("2024-11-30T10:15:30Z", event.getCreatedAt());
    }

    // ==========================
    // Null Field Tests
    // ==========================

    @Test
    void testAllFieldsAreNullByDefault() {
        assertNull(event.getId());
        assertNull(event.getEventType());
        assertNull(event.getPriority());
        assertNull(event.getSubjectId());
        assertNull(event.getSubjectType());
        assertNull(event.getSubjectUrl());
        assertNull(event.getDescription());
        assertNull(event.getCreatedAt());
    }

    @Test
    void testSetFieldToNull_OverwritesPreviousValue() {
        event.setId("event-001");
        event.setId(null);
        assertNull(event.getId());
    }

    @Test
    void testSetFieldToEmptyString() {
        event.setEventType("");
        assertEquals("", event.getEventType());
    }

    // ==========================
    // JSON Deserialization Tests (@JsonProperty)
    // ==========================

    @Test
    void testDeserialization_AllFields() throws Exception {
        String json = """
                {
                    "id": "evt-001",
                    "event_type": "SLIDE_DOWNLOAD_COMPLETED",
                    "priority": "NORMAL",
                    "subject_id": "slide-456",
                    "subject_type": "SLIDE",
                    "subject_url": "https://example.com/slides/456",
                    "description": "Download completed",
                    "created_at": "2024-11-30T12:00:00Z"
                }
                """;

        Event deserialized = objectMapper.readValue(json, Event.class);

        assertEquals("evt-001", deserialized.getId());
        assertEquals("SLIDE_DOWNLOAD_COMPLETED", deserialized.getEventType());
        assertEquals("NORMAL", deserialized.getPriority());
        assertEquals("slide-456", deserialized.getSubjectId());
        assertEquals("SLIDE", deserialized.getSubjectType());
        assertEquals("https://example.com/slides/456", deserialized.getSubjectUrl());
        assertEquals("Download completed", deserialized.getDescription());
        assertEquals("2024-11-30T12:00:00Z", deserialized.getCreatedAt());
    }

    @Test
    void testDeserialization_EventTypeSnakeCase() throws Exception {
        String json = "{\"event_type\": \"CLASSIFICATION_FAILED\"}";
        Event deserialized = objectMapper.readValue(json, Event.class);
        assertEquals("CLASSIFICATION_FAILED", deserialized.getEventType());
    }

    @Test
    void testDeserialization_SubjectIdSnakeCase() throws Exception {
        String json = "{\"subject_id\": \"slide-789\"}";
        Event deserialized = objectMapper.readValue(json, Event.class);
        assertEquals("slide-789", deserialized.getSubjectId());
    }

    @Test
    void testDeserialization_SubjectTypeSnakeCase() throws Exception {
        String json = "{\"subject_type\": \"CASE\"}";
        Event deserialized = objectMapper.readValue(json, Event.class);
        assertEquals("CASE", deserialized.getSubjectType());
    }

    @Test
    void testDeserialization_SubjectUrlSnakeCase() throws Exception {
        String json = "{\"subject_url\": \"https://example.com/cases/1\"}";
        Event deserialized = objectMapper.readValue(json, Event.class);
        assertEquals("https://example.com/cases/1", deserialized.getSubjectUrl());
    }

    @Test
    void testDeserialization_CreatedAtSnakeCase() throws Exception {
        String json = "{\"created_at\": \"2024-12-01T08:00:00Z\"}";
        Event deserialized = objectMapper.readValue(json, Event.class);
        assertEquals("2024-12-01T08:00:00Z", deserialized.getCreatedAt());
    }

    @Test
    void testDeserialization_MissingFields_DefaultToNull() throws Exception {
        String json = "{\"id\": \"evt-002\"}";
        Event deserialized = objectMapper.readValue(json, Event.class);

        assertEquals("evt-002", deserialized.getId());
        assertNull(deserialized.getEventType());
        assertNull(deserialized.getSubjectId());
        assertNull(deserialized.getSubjectType());
        assertNull(deserialized.getSubjectUrl());
        assertNull(deserialized.getDescription());
        assertNull(deserialized.getCreatedAt());
    }

    @Test
    void testDeserialization_EmptyJson_AllFieldsNull() throws Exception {
        String json = "{}";
        Event deserialized = objectMapper.readValue(json, Event.class);

        assertNull(deserialized.getId());
        assertNull(deserialized.getEventType());
        assertNull(deserialized.getPriority());
        assertNull(deserialized.getSubjectId());
        assertNull(deserialized.getSubjectType());
        assertNull(deserialized.getSubjectUrl());
        assertNull(deserialized.getDescription());
        assertNull(deserialized.getCreatedAt());
    }

    @Test
    void testDeserialization_NullValues_InJson() throws Exception {
        String json = """
                {
                    "id": null,
                    "event_type": null,
                    "subject_id": null
                }
                """;

        Event deserialized = objectMapper.readValue(json, Event.class);

        assertNull(deserialized.getId());
        assertNull(deserialized.getEventType());
        assertNull(deserialized.getSubjectId());
    }

    // ==========================
    // JSON Serialization Tests
    // ==========================

    @Test
    void testSerialization_EventTypeUsesSnakeCaseKey() throws Exception {
        event.setEventType("REPORT_SUBMITTED");
        String json = objectMapper.writeValueAsString(event);
        assertTrue(json.contains("event_type"));
        assertFalse(json.contains("eventType"));
    }

    @Test
    void testSerialization_SubjectIdUsesSnakeCaseKey() throws Exception {
        event.setSubjectId("slide-001");
        String json = objectMapper.writeValueAsString(event);
        assertTrue(json.contains("subject_id"));
        assertFalse(json.contains("subjectId"));
    }

    @Test
    void testSerialization_SubjectTypeUsesSnakeCaseKey() throws Exception {
        event.setSubjectType("SLIDE");
        String json = objectMapper.writeValueAsString(event);
        assertTrue(json.contains("subject_type"));
        assertFalse(json.contains("subjectType"));
    }

    @Test
    void testSerialization_SubjectUrlUsesSnakeCaseKey() throws Exception {
        event.setSubjectUrl("https://example.com");
        String json = objectMapper.writeValueAsString(event);
        assertTrue(json.contains("subject_url"));
        assertFalse(json.contains("subjectUrl"));
    }

    @Test
    void testSerialization_CreatedAtUsesSnakeCaseKey() throws Exception {
        event.setCreatedAt("2024-11-30T10:00:00Z");
        String json = objectMapper.writeValueAsString(event);
        assertTrue(json.contains("created_at"));
        assertFalse(json.contains("createdAt"));
    }

    @Test
    void testSerialization_AllFields() throws Exception {
        event.setId("evt-999");
        event.setEventType("INVALID_SLIDE");
        event.setPriority("LOW");
        event.setSubjectId("slide-999");
        event.setSubjectType("SLIDE");
        event.setSubjectUrl("https://example.com/slides/999");
        event.setDescription("Slide is invalid");
        event.setCreatedAt("2024-12-01T00:00:00Z");

        String json = objectMapper.writeValueAsString(event);

        assertTrue(json.contains("evt-999"));
        assertTrue(json.contains("INVALID_SLIDE"));
        assertTrue(json.contains("LOW"));
        assertTrue(json.contains("slide-999"));
        assertTrue(json.contains("SLIDE"));
        assertTrue(json.contains("https://example.com/slides/999"));
        assertTrue(json.contains("Slide is invalid"));
        assertTrue(json.contains("2024-12-01T00:00:00Z"));
    }

    // ==========================
    // Roundtrip Tests
    // ==========================

    @Test
    void testRoundtrip_SerializeAndDeserialize() throws Exception {
        event.setId("evt-roundtrip");
        event.setEventType("CLASSIFICATION_FINISHED");
        event.setPriority("NORMAL");
        event.setSubjectId("slide-rt");
        event.setSubjectType("SLIDE");
        event.setSubjectUrl("https://example.com/rt");
        event.setDescription("Roundtrip test");
        event.setCreatedAt("2024-12-01T06:00:00Z");

        String json = objectMapper.writeValueAsString(event);
        Event deserialized = objectMapper.readValue(json, Event.class);

        assertEquals(event.getId(), deserialized.getId());
        assertEquals(event.getEventType(), deserialized.getEventType());
        assertEquals(event.getPriority(), deserialized.getPriority());
        assertEquals(event.getSubjectId(), deserialized.getSubjectId());
        assertEquals(event.getSubjectType(), deserialized.getSubjectType());
        assertEquals(event.getSubjectUrl(), deserialized.getSubjectUrl());
        assertEquals(event.getDescription(), deserialized.getDescription());
        assertEquals(event.getCreatedAt(), deserialized.getCreatedAt());
    }
}