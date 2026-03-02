package com.eh.digitalpathology.ibex.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class WebClientConfigTest {

    @Test
    void webClient_createsWebClientWithBaseUrl() {
        WebClientConfig config = new WebClientConfig();
        ReflectionTestUtils.setField(config, "baseUrl", "http://localhost:8080");

        WebClient webClient = config.webClient();

        assertNotNull(webClient);
    }
}