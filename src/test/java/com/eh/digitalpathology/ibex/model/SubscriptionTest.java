package com.eh.digitalpathology.ibex.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionTest {

    private Subscription subscription;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        subscription = new Subscription();
    }

    // ==========================
    // Constructor Tests
    // ==========================

    @Test
    void testDefaultConstructor_ActiveIsFalseByDefault() {
        assertFalse(subscription.isActive());
    }

    @Test
    void testDefaultConstructor_StringFieldsAreNullByDefault() {
        assertNull(subscription.getCallbackUrl());
        assertNull(subscription.getApiKey());
        assertNull(subscription.getDataKey());
    }

    // ==========================
    // Getter & Setter Tests
    // ==========================

    @Test
    void testSetAndGetActive_True() {
        subscription.setActive(true);
        assertTrue(subscription.isActive());
    }

    @Test
    void testSetAndGetActive_False() {
        subscription.setActive(true);
        subscription.setActive(false);
        assertFalse(subscription.isActive());
    }

    @Test
    void testSetAndGetCallbackUrl() {
        subscription.setCallbackUrl("https://example.com/webhook");
        assertEquals("https://example.com/webhook", subscription.getCallbackUrl());
    }

    @Test
    void testSetAndGetApiKey() {
        subscription.setApiKey("api-key-abc123");
        assertEquals("api-key-abc123", subscription.getApiKey());
    }

    @Test
    void testSetAndGetDataKey() {
        subscription.setDataKey("data-key-xyz456");
        assertEquals("data-key-xyz456", subscription.getDataKey());
    }

    @Test
    void testSetCallbackUrl_ToNull() {
        subscription.setCallbackUrl("https://example.com/webhook");
        subscription.setCallbackUrl(null);
        assertNull(subscription.getCallbackUrl());
    }

    @Test
    void testSetApiKey_ToNull() {
        subscription.setApiKey("api-key-abc123");
        subscription.setApiKey(null);
        assertNull(subscription.getApiKey());
    }

    @Test
    void testSetDataKey_ToNull() {
        subscription.setDataKey("data-key-xyz456");
        subscription.setDataKey(null);
        assertNull(subscription.getDataKey());
    }

    @Test
    void testSetCallbackUrl_ToEmptyString() {
        subscription.setCallbackUrl("");
        assertEquals("", subscription.getCallbackUrl());
    }

    @Test
    void testSetApiKey_ToEmptyString() {
        subscription.setApiKey("");
        assertEquals("", subscription.getApiKey());
    }

    @Test
    void testSetDataKey_ToEmptyString() {
        subscription.setDataKey("");
        assertEquals("", subscription.getDataKey());
    }

    @Test
    void testSetCallbackUrl_OverwritesPreviousValue() {
        subscription.setCallbackUrl("https://first.com");
        subscription.setCallbackUrl("https://second.com");
        assertEquals("https://second.com", subscription.getCallbackUrl());
    }

    // ==========================
    // JSON Deserialization Tests
    // ==========================

    @Test
    void testDeserialization_AllFields() throws Exception {
        String json = """
                {
                    "active": true,
                    "callback_url": "https://example.com/webhook",
                    "api_key": "api-key-123",
                    "data_key": "data-key-456"
                }
                """;

        Subscription deserialized = objectMapper.readValue(json, Subscription.class);

        assertTrue(deserialized.isActive());
        assertEquals("https://example.com/webhook", deserialized.getCallbackUrl());
        assertEquals("api-key-123", deserialized.getApiKey());
        assertEquals("data-key-456", deserialized.getDataKey());
    }

    @Test
    void testDeserialization_ActiveFalse() throws Exception {
        String json = "{\"active\": false, \"callback_url\": \"https://example.com\"}";
        Subscription deserialized = objectMapper.readValue(json, Subscription.class);

        assertFalse(deserialized.isActive());
    }

    @Test
    void testDeserialization_CallbackUrlSnakeCaseKey() throws Exception {
        String json = "{\"callback_url\": \"https://example.com/cb\"}";
        Subscription deserialized = objectMapper.readValue(json, Subscription.class);

        assertEquals("https://example.com/cb", deserialized.getCallbackUrl());
    }

    @Test
    void testDeserialization_ApiKeySnakeCaseKey() throws Exception {
        String json = "{\"api_key\": \"my-api-key\"}";
        Subscription deserialized = objectMapper.readValue(json, Subscription.class);

        assertEquals("my-api-key", deserialized.getApiKey());
    }

    @Test
    void testDeserialization_DataKeySnakeCaseKey() throws Exception {
        String json = "{\"data_key\": \"my-data-key\"}";
        Subscription deserialized = objectMapper.readValue(json, Subscription.class);

        assertEquals("my-data-key", deserialized.getDataKey());
    }

    @Test
    void testDeserialization_MissingOptionalFields_DefaultToNull() throws Exception {
        String json = "{\"active\": true, \"callback_url\": \"https://example.com\"}";
        Subscription deserialized = objectMapper.readValue(json, Subscription.class);

        assertTrue(deserialized.isActive());
        assertEquals("https://example.com", deserialized.getCallbackUrl());
        assertNull(deserialized.getApiKey());
        assertNull(deserialized.getDataKey());
    }

    @Test
    void testDeserialization_EmptyJson_ActiveDefaultsFalse() throws Exception {
        String json = "{}";
        Subscription deserialized = objectMapper.readValue(json, Subscription.class);

        assertFalse(deserialized.isActive());
    }

    @Test
    void testDeserialization_EmptyJson_StringFieldsNull() throws Exception {
        String json = "{}";
        Subscription deserialized = objectMapper.readValue(json, Subscription.class);

        assertNull(deserialized.getCallbackUrl());
        assertNull(deserialized.getApiKey());
        assertNull(deserialized.getDataKey());
    }

    @Test
    void testDeserialization_NullOptionalFields_InJson() throws Exception {
        String json = """
                {
                    "active": true,
                    "callback_url": "https://example.com",
                    "api_key": null,
                    "data_key": null
                }
                """;

        Subscription deserialized = objectMapper.readValue(json, Subscription.class);

        assertNull(deserialized.getApiKey());
        assertNull(deserialized.getDataKey());
    }

    // ==========================
    // JSON Serialization Tests
    // ==========================

    @Test
    void testSerialization_CallbackUrlUsesSnakeCaseKey() throws Exception {
        subscription.setCallbackUrl("https://example.com/webhook");
        String json = objectMapper.writeValueAsString(subscription);

        assertTrue(json.contains("callback_url"));
        assertFalse(json.contains("callbackUrl"));
    }

    @Test
    void testSerialization_ApiKeyUsesSnakeCaseKey() throws Exception {
        subscription.setApiKey("my-api-key");
        String json = objectMapper.writeValueAsString(subscription);

        assertTrue(json.contains("api_key"));
        assertFalse(json.contains("apiKey"));
    }

    @Test
    void testSerialization_DataKeyUsesSnakeCaseKey() throws Exception {
        subscription.setDataKey("my-data-key");
        String json = objectMapper.writeValueAsString(subscription);

        assertTrue(json.contains("data_key"));
        assertFalse(json.contains("dataKey"));
    }

    @Test
    void testSerialization_ApiKeyOmitted_WhenNull() throws Exception {
        subscription.setApiKey(null);
        String json = objectMapper.writeValueAsString(subscription);

        assertFalse(json.contains("api_key"));
    }

    @Test
    void testSerialization_ApiKeyIncluded_WhenPresent() throws Exception {
        subscription.setApiKey("api-key-abc");
        String json = objectMapper.writeValueAsString(subscription);

        assertTrue(json.contains("api_key"));
        assertTrue(json.contains("api-key-abc"));
    }

    @Test
    void testSerialization_ApiKeyIncluded_WhenEmpty() throws Exception {
        // NON_NULL (not NON_EMPTY) — empty string must still be serialized
        subscription.setApiKey("");
        String json = objectMapper.writeValueAsString(subscription);

        assertTrue(json.contains("api_key"));
    }

    @Test
    void testSerialization_DataKeyOmitted_WhenNull() throws Exception {
        subscription.setDataKey(null);
        String json = objectMapper.writeValueAsString(subscription);

        assertFalse(json.contains("data_key"));
    }

    @Test
    void testSerialization_DataKeyIncluded_WhenPresent() throws Exception {
        subscription.setDataKey("data-key-xyz");
        String json = objectMapper.writeValueAsString(subscription);

        assertTrue(json.contains("data_key"));
        assertTrue(json.contains("data-key-xyz"));
    }

    @Test
    void testSerialization_DataKeyIncluded_WhenEmpty() throws Exception {
        // NON_NULL (not NON_EMPTY) — empty string must still be serialized
        subscription.setDataKey("");
        String json = objectMapper.writeValueAsString(subscription);

        assertTrue(json.contains("data_key"));
    }

    @Test
    void testSerialization_ActiveTrue_SerializedCorrectly() throws Exception {
        subscription.setActive(true);
        String json = objectMapper.writeValueAsString(subscription);

        assertTrue(json.contains("\"active\":true"));
    }

    @Test
    void testSerialization_ActiveFalse_SerializedCorrectly() throws Exception {
        subscription.setActive(false);
        String json = objectMapper.writeValueAsString(subscription);

        assertTrue(json.contains("\"active\":false"));
    }

    @Test
    void testSerialization_AllFields() throws Exception {
        subscription.setActive(true);
        subscription.setCallbackUrl("https://example.com/webhook");
        subscription.setApiKey("api-key-123");
        subscription.setDataKey("data-key-456");

        String json = objectMapper.writeValueAsString(subscription);

        assertTrue(json.contains("true"));
        assertTrue(json.contains("https://example.com/webhook"));
        assertTrue(json.contains("api-key-123"));
        assertTrue(json.contains("data-key-456"));
    }

    // ==========================
    // Roundtrip Tests
    // ==========================

    @Test
    void testRoundtrip_SerializeAndDeserialize_AllFields() throws Exception {
        subscription.setActive(true);
        subscription.setCallbackUrl("https://example.com/webhook");
        subscription.setApiKey("api-key-rt");
        subscription.setDataKey("data-key-rt");

        String json = objectMapper.writeValueAsString(subscription);
        Subscription deserialized = objectMapper.readValue(json, Subscription.class);

        assertEquals(subscription.isActive(), deserialized.isActive());
        assertEquals(subscription.getCallbackUrl(), deserialized.getCallbackUrl());
        assertEquals(subscription.getApiKey(), deserialized.getApiKey());
        assertEquals(subscription.getDataKey(), deserialized.getDataKey());
    }

    @Test
    void testRoundtrip_WithNullOptionalFields() throws Exception {
        subscription.setActive(false);
        subscription.setCallbackUrl("https://example.com/webhook");

        String json = objectMapper.writeValueAsString(subscription);
        Subscription deserialized = objectMapper.readValue(json, Subscription.class);

        assertFalse(deserialized.isActive());
        assertEquals("https://example.com/webhook", deserialized.getCallbackUrl());
        assertNull(deserialized.getApiKey());
        assertNull(deserialized.getDataKey());
    }

    @Test
    void testRoundtrip_ActiveFalse_PreservedCorrectly() throws Exception {
        subscription.setActive(false);
        subscription.setCallbackUrl("https://example.com");

        String json = objectMapper.writeValueAsString(subscription);
        Subscription deserialized = objectMapper.readValue(json, Subscription.class);

        assertFalse(deserialized.isActive());
    }
}