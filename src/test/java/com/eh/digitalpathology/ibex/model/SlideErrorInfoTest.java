package com.eh.digitalpathology.ibex.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlideErrorInfoTest {

    // ==========================
    // Constructor & Accessor Tests
    // ==========================

    @Test
    void testConstructorAndAccessors_AllFields() {
        SlideErrorInfo info = new SlideErrorInfo("barcode-001", 404, "Slide not found");

        assertEquals("barcode-001", info.barcode());
        assertEquals(404, info.errorCode());
        assertEquals("Slide not found", info.errorMsg());
    }

    @Test
    void testConstructorAndAccessors_NullBarcode() {
        SlideErrorInfo info = new SlideErrorInfo(null, 500, "Internal error");

        assertNull(info.barcode());
        assertEquals(500, info.errorCode());
        assertEquals("Internal error", info.errorMsg());
    }

    @Test
    void testConstructorAndAccessors_NullErrorMsg() {
        SlideErrorInfo info = new SlideErrorInfo("barcode-002", 400, null);

        assertEquals("barcode-002", info.barcode());
        assertEquals(400, info.errorCode());
        assertNull(info.errorMsg());
    }

    @Test
    void testConstructorAndAccessors_BothStringsNull() {
        SlideErrorInfo info = new SlideErrorInfo(null, 0, null);

        assertNull(info.barcode());
        assertEquals(0, info.errorCode());
        assertNull(info.errorMsg());
    }

    @Test
    void testConstructorAndAccessors_EmptyBarcode() {
        SlideErrorInfo info = new SlideErrorInfo("", 422, "Unprocessable");

        assertEquals("", info.barcode());
        assertEquals(422, info.errorCode());
        assertEquals("Unprocessable", info.errorMsg());
    }

    @Test
    void testConstructorAndAccessors_EmptyErrorMsg() {
        SlideErrorInfo info = new SlideErrorInfo("barcode-003", 200, "");

        assertEquals("barcode-003", info.barcode());
        assertEquals(200, info.errorCode());
        assertEquals("", info.errorMsg());
    }

    @Test
    void testConstructorAndAccessors_ZeroErrorCode() {
        SlideErrorInfo info = new SlideErrorInfo("barcode-004", 0, "No error");

        assertEquals("barcode-004", info.barcode());
        assertEquals(0, info.errorCode());
        assertEquals("No error", info.errorMsg());
    }

    @Test
    void testConstructorAndAccessors_NegativeErrorCode() {
        SlideErrorInfo info = new SlideErrorInfo("barcode-005", -1, "Unknown error");

        assertEquals("barcode-005", info.barcode());
        assertEquals(-1, info.errorCode());
        assertEquals("Unknown error", info.errorMsg());
    }

    @Test
    void testConstructorAndAccessors_LargeErrorCode() {
        SlideErrorInfo info = new SlideErrorInfo("barcode-006", Integer.MAX_VALUE, "Max error");

        assertEquals(Integer.MAX_VALUE, info.errorCode());
    }

    @Test
    void testConstructorAndAccessors_CommonHttpErrorCodes() {
        assertDoesNotThrow(() -> new SlideErrorInfo("bc", 200, "OK"));
        assertDoesNotThrow(() -> new SlideErrorInfo("bc", 201, "Created"));
        assertDoesNotThrow(() -> new SlideErrorInfo("bc", 400, "Bad Request"));
        assertDoesNotThrow(() -> new SlideErrorInfo("bc", 401, "Unauthorized"));
        assertDoesNotThrow(() -> new SlideErrorInfo("bc", 403, "Forbidden"));
        assertDoesNotThrow(() -> new SlideErrorInfo("bc", 404, "Not Found"));
        assertDoesNotThrow(() -> new SlideErrorInfo("bc", 500, "Internal Server Error"));
        assertDoesNotThrow(() -> new SlideErrorInfo("bc", 503, "Service Unavailable"));
    }

    // ==========================
    // equals() Tests
    // ==========================

    @Test
    void testEquals_SameValues() {
        SlideErrorInfo i1 = new SlideErrorInfo("barcode-001", 404, "Not Found");
        SlideErrorInfo i2 = new SlideErrorInfo("barcode-001", 404, "Not Found");

        assertEquals(i1, i2);
    }

    @Test
    void testEquals_DifferentBarcode() {
        SlideErrorInfo i1 = new SlideErrorInfo("barcode-001", 404, "Not Found");
        SlideErrorInfo i2 = new SlideErrorInfo("barcode-002", 404, "Not Found");

        assertNotEquals(i1, i2);
    }

    @Test
    void testEquals_DifferentErrorCode() {
        SlideErrorInfo i1 = new SlideErrorInfo("barcode-001", 404, "Not Found");
        SlideErrorInfo i2 = new SlideErrorInfo("barcode-001", 500, "Not Found");

        assertNotEquals(i1, i2);
    }

    @Test
    void testEquals_DifferentErrorMsg() {
        SlideErrorInfo i1 = new SlideErrorInfo("barcode-001", 404, "Not Found");
        SlideErrorInfo i2 = new SlideErrorInfo("barcode-001", 404, "Slide Missing");

        assertNotEquals(i1, i2);
    }

    @Test
    void testEquals_BothNullStringFields() {
        SlideErrorInfo i1 = new SlideErrorInfo(null, 0, null);
        SlideErrorInfo i2 = new SlideErrorInfo(null, 0, null);

        assertEquals(i1, i2);
    }

    @Test
    void testEquals_ComparedToNull() {
        SlideErrorInfo i1 = new SlideErrorInfo("barcode-001", 404, "Not Found");

        assertNotEquals(null, i1);
    }

    @Test
    void testEquals_SameInstance() {
        SlideErrorInfo i1 = new SlideErrorInfo("barcode-001", 404, "Not Found");

        assertEquals(i1, i1);
    }

    @Test
    void testEquals_NullBarcodeOneNullOneNot() {
        SlideErrorInfo i1 = new SlideErrorInfo(null, 404, "Not Found");
        SlideErrorInfo i2 = new SlideErrorInfo("barcode-001", 404, "Not Found");

        assertNotEquals(i1, i2);
    }

    // ==========================
    // hashCode() Tests
    // ==========================

    @Test
    void testHashCode_EqualObjectsHaveSameHashCode() {
        SlideErrorInfo i1 = new SlideErrorInfo("barcode-001", 404, "Not Found");
        SlideErrorInfo i2 = new SlideErrorInfo("barcode-001", 404, "Not Found");

        assertEquals(i1.hashCode(), i2.hashCode());
    }

    @Test
    void testHashCode_DifferentObjectsLikelyDifferentHashCode() {
        SlideErrorInfo i1 = new SlideErrorInfo("barcode-001", 404, "Not Found");
        SlideErrorInfo i2 = new SlideErrorInfo("barcode-999", 500, "Server Error");

        assertNotEquals(i1.hashCode(), i2.hashCode());
    }

    @Test
    void testHashCode_NullFieldsDoNotThrow() {
        SlideErrorInfo info = new SlideErrorInfo(null, 0, null);

        assertDoesNotThrow(info::hashCode);
    }

    @Test
    void testHashCode_IsConsistentAcrossMultipleCalls() {
        SlideErrorInfo info = new SlideErrorInfo("barcode-001", 404, "Not Found");

        assertEquals(info.hashCode(), info.hashCode());
    }

    // ==========================
    // toString() Tests
    // ==========================

    @Test
    void testToString_ContainsBarcode() {
        SlideErrorInfo info = new SlideErrorInfo("barcode-001", 404, "Not Found");

        assertTrue(info.toString().contains("barcode-001"));
    }

    @Test
    void testToString_ContainsErrorCode() {
        SlideErrorInfo info = new SlideErrorInfo("barcode-001", 404, "Not Found");

        assertTrue(info.toString().contains("404"));
    }

    @Test
    void testToString_ContainsErrorMsg() {
        SlideErrorInfo info = new SlideErrorInfo("barcode-001", 404, "Not Found");

        assertTrue(info.toString().contains("Not Found"));
    }

    @Test
    void testToString_ContainsAllFields() {
        SlideErrorInfo info = new SlideErrorInfo("barcode-xyz", 500, "Internal Error");
        String result = info.toString();

        assertTrue(result.contains("barcode-xyz"));
        assertTrue(result.contains("500"));
        assertTrue(result.contains("Internal Error"));
    }

    @Test
    void testToString_NullFieldsDoNotThrow() {
        SlideErrorInfo info = new SlideErrorInfo(null, 0, null);

        assertDoesNotThrow(info::toString);
    }
}