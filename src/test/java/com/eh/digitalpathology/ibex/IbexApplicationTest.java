package com.eh.digitalpathology.ibex;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class IbexApplicationTest {

    @Test
    void mainMethodShouldNotThrow() {
        try (var mockedSpringApp = mockStatic(org.springframework.boot.SpringApplication.class)) {
            mockedSpringApp.when(() ->
                            org.springframework.boot.SpringApplication.run(
                                    IbexApplication.class, new String[]{}))
                    .thenReturn(null);

            assertDoesNotThrow(() -> IbexApplication.main(new String[]{}));
        }
    }
}