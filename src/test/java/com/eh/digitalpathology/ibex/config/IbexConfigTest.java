package com.eh.digitalpathology.ibex.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IbexConfigTest {

    @Test
    void testSetAndGetCallbackUrl() {
        IbexConfig config = new IbexConfig();

        config.setCallbackUrl("https://callback-url");

        assertEquals("https://callback-url", config.getCallbackUrl());
    }

    @Test
    void testSetAndGetApiObject() {
        IbexConfig config = new IbexConfig();
        IbexConfig.Api api = new IbexConfig.Api();

        api.setUrl("https://api-url");
        api.setKey("secret-key");

        config.setApi(api);

        assertNotNull(config.getApi());
        assertEquals("https://api-url", config.getApi().getUrl());
        assertEquals("secret-key", config.getApi().getKey());
    }

    @Test
    void testApiClassSettersAndGetters() {
        IbexConfig.Api api = new IbexConfig.Api();

        api.setUrl("https://test-url");
        api.setKey("test-key");

        assertEquals("https://test-url", api.getUrl());
        assertEquals("test-key", api.getKey());
    }

    @Test
    void testObjectCreation() {
        IbexConfig config = new IbexConfig();
        IbexConfig.Api api = new IbexConfig.Api();

        assertNotNull(config);
        assertNotNull(api);
    }
}