package com.eh.digitalpathology.ibex.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GcpConfigTest {

    @Test
    void testSetAndGetCreds() {
        GcpConfig config = new GcpConfig();

        config.setCreds("test-credentials");

        assertEquals("test-credentials", config.getCreds());
    }

    @Test
    void testSetAndGetDicomWebUrl() {
        GcpConfig config = new GcpConfig();

        config.setDicomWebUrl("https://dicom-web-url");

        assertEquals("https://dicom-web-url", config.getDicomWebUrl());
    }

    @Test
    void testObjectCreation() {
        GcpConfig config = new GcpConfig();
        assertNotNull(config);
    }
}