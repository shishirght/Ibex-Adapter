package com.eh.digitalpathology.ibex.main;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.cloud.config.enabled=false")
@Disabled("Temporarily skipping all context tests")
class IbexApplicationTests {
    @Autowired
    private ApplicationContext applicationContext;

    @Test

    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

}
