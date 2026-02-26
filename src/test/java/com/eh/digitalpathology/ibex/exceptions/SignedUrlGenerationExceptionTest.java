package com.eh.digitalpathology.ibex.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignedUrlGenerationExceptionTest {

    @Test
    void testConstructor_ShouldSetMessageAndCause() {
        // Arrange
        String errorMessage = "Error while generating signed URL";
        Throwable cause = new RuntimeException("Root cause");

        // Act
        SignedUrlGenerationException exception =
                new SignedUrlGenerationException(errorMessage, cause);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testException_ShouldBeInstanceOfRuntimeException() {
        SignedUrlGenerationException exception =
                new SignedUrlGenerationException("Test message", new Exception());

        assertInstanceOf(RuntimeException.class, exception);
    }
}