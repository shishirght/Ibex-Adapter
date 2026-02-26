package com.eh.digitalpathology.ibex.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    // ==========================
    // Constructor & Accessor Tests
    // ==========================

    @Test
    void testApiResponse_AllFieldsSet() {
        ApiResponse<String> response = new ApiResponse<>("SUCCESS", "some-content", "200", "OK");

        assertEquals("SUCCESS", response.status());
        assertEquals("some-content", response.content());
        assertEquals("200", response.errorCode());
        assertEquals("OK", response.errorMessage());
    }

    @Test
    void testApiResponse_NullFields() {
        ApiResponse<String> response = new ApiResponse<>(null, null, null, null);

        assertNull(response.status());
        assertNull(response.content());
        assertNull(response.errorCode());
        assertNull(response.errorMessage());
    }

    @Test
    void testApiResponse_WithIntegerContent() {
        ApiResponse<Integer> response = new ApiResponse<>("SUCCESS", 42, null, null);

        assertEquals("SUCCESS", response.status());
        assertEquals(42, response.content());
    }

    @Test
    void testApiResponse_WithListContent() {
        java.util.List<String> contentList = java.util.List.of("item1", "item2");
        ApiResponse<java.util.List<String>> response = new ApiResponse<>("SUCCESS", contentList, null, null);

        assertEquals(contentList, response.content());
        assertEquals(2, response.content().size());
    }

    @Test
    void testApiResponse_WithObjectContent() {
        ApiResponse<Object> response = new ApiResponse<>("ERROR", new Object(), "500", "Internal Server Error");

        assertEquals("ERROR", response.status());
        assertEquals("500", response.errorCode());
        assertEquals("Internal Server Error", response.errorMessage());
        assertNotNull(response.content());
    }

    @Test
    void testApiResponse_ErrorScenario_ContentNull() {
        ApiResponse<String> response = new ApiResponse<>("ERROR", null, "404", "Not Found");

        assertEquals("ERROR", response.status());
        assertNull(response.content());
        assertEquals("404", response.errorCode());
        assertEquals("Not Found", response.errorMessage());
    }

    // ==========================
    // equals() Tests
    // ==========================

    @Test
    void testApiResponse_Equals_WhenSameValues() {
        ApiResponse<String> r1 = new ApiResponse<>("SUCCESS", "content", "200", "OK");
        ApiResponse<String> r2 = new ApiResponse<>("SUCCESS", "content", "200", "OK");

        assertEquals(r1, r2);
    }

    @Test
    void testApiResponse_NotEquals_WhenStatusDiffers() {
        ApiResponse<String> r1 = new ApiResponse<>("SUCCESS", "content", "200", "OK");
        ApiResponse<String> r2 = new ApiResponse<>("ERROR", "content", "200", "OK");

        assertNotEquals(r1, r2);
    }

    @Test
    void testApiResponse_NotEquals_WhenContentDiffers() {
        ApiResponse<String> r1 = new ApiResponse<>("SUCCESS", "content-a", "200", "OK");
        ApiResponse<String> r2 = new ApiResponse<>("SUCCESS", "content-b", "200", "OK");

        assertNotEquals(r1, r2);
    }

    @Test
    void testApiResponse_NotEquals_WhenErrorCodeDiffers() {
        ApiResponse<String> r1 = new ApiResponse<>("ERROR", null, "404", "Not Found");
        ApiResponse<String> r2 = new ApiResponse<>("ERROR", null, "500", "Not Found");

        assertNotEquals(r1, r2);
    }

    @Test
    void testApiResponse_NotEquals_WhenErrorMessageDiffers() {
        ApiResponse<String> r1 = new ApiResponse<>("ERROR", null, "500", "Server Error");
        ApiResponse<String> r2 = new ApiResponse<>("ERROR", null, "500", "Internal Error");

        assertNotEquals(r1, r2);
    }

    @Test
    void testApiResponse_NotEquals_Null() {
        ApiResponse<String> r1 = new ApiResponse<>("SUCCESS", "content", "200", "OK");

        assertNotEquals(null, r1);
    }

    @Test
    void testApiResponse_Equals_BothNullFields() {
        ApiResponse<String> r1 = new ApiResponse<>(null, null, null, null);
        ApiResponse<String> r2 = new ApiResponse<>(null, null, null, null);

        assertEquals(r1, r2);
    }

    // ==========================
    // hashCode() Tests
    // ==========================

    @Test
    void testApiResponse_HashCode_EqualObjectsHaveSameHashCode() {
        ApiResponse<String> r1 = new ApiResponse<>("SUCCESS", "content", "200", "OK");
        ApiResponse<String> r2 = new ApiResponse<>("SUCCESS", "content", "200", "OK");

        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testApiResponse_HashCode_DifferentObjectsLikelyDifferentHashCode() {
        ApiResponse<String> r1 = new ApiResponse<>("SUCCESS", "content-a", "200", "OK");
        ApiResponse<String> r2 = new ApiResponse<>("ERROR", "content-b", "500", "Fail");

        assertNotEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testApiResponse_HashCode_NullFields_DoesNotThrow() {
        ApiResponse<String> r = new ApiResponse<>(null, null, null, null);
        assertDoesNotThrow(r::hashCode);
    }

    // ==========================
    // toString() Tests
    // ==========================

    @Test
    void testApiResponse_ToString_ContainsAllFields() {
        ApiResponse<String> response = new ApiResponse<>("SUCCESS", "my-content", "200", "OK");
        String result = response.toString();

        assertTrue(result.contains("SUCCESS"));
        assertTrue(result.contains("my-content"));
        assertTrue(result.contains("200"));
        assertTrue(result.contains("OK"));
    }

    @Test
    void testApiResponse_ToString_HandlesNullFields() {
        ApiResponse<String> response = new ApiResponse<>(null, null, null, null);
        assertDoesNotThrow(response::toString);
    }
}