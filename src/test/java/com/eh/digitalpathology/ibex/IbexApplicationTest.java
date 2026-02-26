package com.eh.digitalpathology.ibex;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class IbexApplicationTest {

    @Test
    @Disabled
    void mainMethodShouldRunWithoutException() {
        assertDoesNotThrow(() ->
                IbexApplication.main(new String[]{}));
    }
}