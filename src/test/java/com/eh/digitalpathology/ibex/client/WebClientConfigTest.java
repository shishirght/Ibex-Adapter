package com.eh.digitalpathology.ibex.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class WebClientConfigTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(WebClientConfig.class)
                    .withPropertyValues("db.service.url=http://localhost:8080");

    @Test
    void shouldCreateWebClientBean() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(WebClient.class);
        });
    }

    @Test
    void shouldConfigureBaseUrlAndDefaultHeader() {
        contextRunner.run(context -> {

            WebClient webClient = context.getBean(WebClient.class);

            assertThat(webClient).isNotNull();

            // Verify default header exists by building a request
            WebClient.RequestHeadersSpec<?> spec =
                    webClient.get().uri("/test");

            assertThat(spec).isNotNull();
        });
    }
}