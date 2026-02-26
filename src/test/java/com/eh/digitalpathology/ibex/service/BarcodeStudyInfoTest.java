package com.eh.digitalpathology.ibex.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeStudyInfoTest {

    private BarcodeStudyInfo barcodeStudyInfo;

    @BeforeEach
    void setUp() {
        barcodeStudyInfo = new BarcodeStudyInfo();
    }

    @Test
    void putAndGet_shouldStoreAndReturnStudyUid() {
        // Arrange
        String barcode = "BC123";
        String studyUid = "1.2.840.113619.2.55.3";

        // Act
        barcodeStudyInfo.put(barcode, studyUid);
        String result = barcodeStudyInfo.get(barcode);

        // Assert
        assertEquals(studyUid, result);
    }

    @Test
    void get_shouldReturnNull_whenBarcodeDoesNotExist() {
        // Act
        String result = barcodeStudyInfo.get("UNKNOWN");

        // Assert
        assertNull(result);
    }

    @Test
    void remove_shouldRemoveAndReturnStudyUid() {
        // Arrange
        String barcode = "BC456";
        String studyUid = "2.16.840.1.113883";

        barcodeStudyInfo.put(barcode, studyUid);

        // Act
        String removed = barcodeStudyInfo.remove(barcode);
        String afterRemove = barcodeStudyInfo.get(barcode);

        // Assert
        assertEquals(studyUid, removed);
        assertNull(afterRemove);
    }

    @Test
    void remove_shouldReturnNull_whenBarcodeDoesNotExist() {
        // Act
        String result = barcodeStudyInfo.remove("NON_EXISTING");

        // Assert
        assertNull(result);
    }
}
