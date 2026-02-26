package com.eh.digitalpathology.ibex.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseWrapperTest {

    // ==========================
    // Constructor & Accessor Tests
    // ==========================

    @Test
    void testConstructorAndAccessors_200WithBody() {
        HttpResponseWrapper wrapper = new HttpResponseWrapper(200, "OK");

        assertEquals(200, wrapper.statusCode());
        assertEquals("OK", wrapper.body());
    }

    @Test
    void testConstructorAndAccessors_404WithBody() {
        HttpResponseWrapper wrapper = new HttpResponseWrapper(404, "Not Found");

        assertEquals(404, wrapper.statusCode());
        assertEquals("Not Found", wrapper.body());
    }

    @Test
    void testConstructorAndAccessors_500WithBody() {
        HttpResponseWrapper wrapper = new HttpResponseWrapper(500, "Internal Server Error");

        assertEquals(500, wrapper.statusCode());
        assertEquals("Internal Server Error", wrapper.body());
    }

    @Test
    void testConstructorAndAccessors_NullBody() {
        HttpResponseWrapper wrapper = new HttpResponseWrapper(200, null);

        assertEquals(200, wrapper.statusCode());
        assertNull(wrapper.body());
    }

    @Test
    void testConstructorAndAccessors_EmptyBody() {
        HttpResponseWrapper wrapper = new HttpResponseWrapper(204, "");

        assertEquals(204, wrapper.statusCode());
        assertEquals("", wrapper.body());
    }

    @Test
    void testConstructorAndAccessors_201WithJsonBody() {
        String jsonBody = "{\"id\":\"123\",\"status\":\"created\"}";
        HttpResponseWrapper wrapper = new HttpResponseWrapper(201, jsonBody);

        assertEquals(201, wrapper.statusCode());
        assertEquals(jsonBody, wrapper.body());
    }

    @Test
    void testConstructorAndAccessors_400WithErrorBody() {
        HttpResponseWrapper wrapper = new HttpResponseWrapper(400, "Bad Request");

        assertEquals(400, wrapper.statusCode());
        assertEquals("Bad Request", wrapper.body());
    }

    @Test
    void testConstructorAndAccessors_ZeroStatusCode() {
        HttpResponseWrapper wrapper = new HttpResponseWrapper(0, null);

        assertEquals(0, wrapper.statusCode());
        assertNull(wrapper.body());
    }

    // ==========================
    // equals() Tests
    // ==========================

    @Test
    void testEquals_SameValues() {
        HttpResponseWrapper w1 = new HttpResponseWrapper(200, "OK");
        HttpResponseWrapper w2 = new HttpResponseWrapper(200, "OK");

        assertEquals(w1, w2);
    }

    @Test
    void testEquals_DifferentStatusCode() {
        HttpResponseWrapper w1 = new HttpResponseWrapper(200, "OK");
        HttpResponseWrapper w2 = new HttpResponseWrapper(404, "OK");

        assertNotEquals(w1, w2);
    }

    @Test
    void testEquals_DifferentBody() {
        HttpResponseWrapper w1 = new HttpResponseWrapper(200, "OK");
        HttpResponseWrapper w2 = new HttpResponseWrapper(200, "Different");

        assertNotEquals(w1, w2);
    }

    @Test
    void testEquals_BothNullBody() {
        HttpResponseWrapper w1 = new HttpResponseWrapper(200, null);
        HttpResponseWrapper w2 = new HttpResponseWrapper(200, null);

        assertEquals(w1, w2);
    }

    @Test
    void testEquals_OneNullBody() {
        HttpResponseWrapper w1 = new HttpResponseWrapper(200, null);
        HttpResponseWrapper w2 = new HttpResponseWrapper(200, "OK");

        assertNotEquals(w1, w2);
    }

    @Test
    void testEquals_ComparedToNull() {
        HttpResponseWrapper w1 = new HttpResponseWrapper(200, "OK");

        assertNotEquals(null, w1);
    }

    @Test
    void testEquals_SameInstance() {
        HttpResponseWrapper w1 = new HttpResponseWrapper(200, "OK");

        assertEquals(w1, w1);
    }

    // ==========================
    // hashCode() Tests
    // ==========================

    @Test
    void testHashCode_EqualObjectsHaveSameHashCode() {
        HttpResponseWrapper w1 = new HttpResponseWrapper(200, "OK");
        HttpResponseWrapper w2 = new HttpResponseWrapper(200, "OK");

        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    void testHashCode_DifferentObjectsLikelyDifferentHashCode() {
        HttpResponseWrapper w1 = new HttpResponseWrapper(200, "OK");
        HttpResponseWrapper w2 = new HttpResponseWrapper(500, "Error");

        assertNotEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    void testHashCode_NullBodyDoesNotThrow() {
        HttpResponseWrapper w = new HttpResponseWrapper(200, null);

        assertDoesNotThrow(w::hashCode);
    }

    @Test
    void testHashCode_IsConsistentAcrossMultipleCalls() {
        HttpResponseWrapper w = new HttpResponseWrapper(200, "OK");

        assertEquals(w.hashCode(), w.hashCode());
    }

    // ==========================
    // toString() Tests
    // ==========================

    @Test
    void testToString_ContainsStatusCode() {
        HttpResponseWrapper w = new HttpResponseWrapper(200, "OK");

        assertTrue(w.toString().contains("200"));
    }

    @Test
    void testToString_ContainsBody() {
        HttpResponseWrapper w = new HttpResponseWrapper(200, "response-body");

        assertTrue(w.toString().contains("response-body"));
    }

    @Test
    void testToString_NullBodyDoesNotThrow() {
        HttpResponseWrapper w = new HttpResponseWrapper(200, null);

        assertDoesNotThrow(w::toString);
    }

    @Test
    void testToString_ContainsBothFields() {
        HttpResponseWrapper w = new HttpResponseWrapper(404, "Not Found");
        String result = w.toString();

        assertTrue(result.contains("404"));
        assertTrue(result.contains("Not Found"));
    }
}