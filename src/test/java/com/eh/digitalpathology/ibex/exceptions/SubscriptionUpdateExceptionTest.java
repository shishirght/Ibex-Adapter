package com.eh.digitalpathology.ibex.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionUpdateExceptionTest {

    @Test
    void testConstructor_ShouldSetMessageAndCause() {
        // Arrange
        String message = "Error while updating subscription";
        Throwable cause = new RuntimeException("Database failure");

        // Act
        SubscriptionUpdateException exception =
                new SubscriptionUpdateException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testShouldBeInstanceOfRuntimeException() {
        SubscriptionUpdateException exception =
                new SubscriptionUpdateException("Test", new Exception());

        assertInstanceOf(RuntimeException.class, exception);
    }
}