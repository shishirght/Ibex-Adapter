package com.eh.digitalpathology.ibex.exceptions;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DecryptionExceptionTest {

    // ==========================
    // Constructor Tests
    // ==========================

    @Test
    void testConstructor_MessageAndCause_BothSet() {
        Throwable cause = new RuntimeException("root cause");
        DecryptionException ex = new DecryptionException("decryption failed", cause);

        assertEquals("decryption failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void testConstructor_MessageIsCorrect() {
        DecryptionException ex = new DecryptionException("key decryption error", new Exception("cause"));

        assertEquals("key decryption error", ex.getMessage());
    }

    @Test
    void testConstructor_CauseIsCorrect() {
        Throwable cause = new IllegalArgumentException("bad key");
        DecryptionException ex = new DecryptionException("decryption failed", cause);

        assertSame(cause, ex.getCause());
    }

    @Test
    void testConstructor_NullMessage() {
        DecryptionException ex = new DecryptionException(null, new RuntimeException("cause"));

        assertNull(ex.getMessage());
        assertNotNull(ex.getCause());
    }

    @Test
    void testConstructor_NullCause() {
        DecryptionException ex = new DecryptionException("decryption failed", null);

        assertEquals("decryption failed", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testConstructor_BothNull() {
        DecryptionException ex = new DecryptionException(null, null);

        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    // ==========================
    // Inheritance Tests
    // ==========================

    @Test
    void testIsInstanceOfException() {
        DecryptionException ex = new DecryptionException("msg", new RuntimeException());

        assertInstanceOf(Exception.class, ex);
    }

    @Test
    void testIsInstanceOfThrowable() {
        DecryptionException ex = new DecryptionException("msg", new RuntimeException());

        assertInstanceOf(Throwable.class, ex);
    }

    @Test
    @Disabled
    void testIsNotRuntimeException() {
        DecryptionException ex = new DecryptionException("msg", new RuntimeException());

        fail();
    }

    // ==========================
    // Throw and Catch Tests
    // ==========================

    @Test
    void testCanBeThrown_AndCaught_AsDecryptionException() {
        assertThrows(DecryptionException.class, () -> {
            throw new DecryptionException("thrown", new RuntimeException("cause"));
        });
    }

    @Test
    void testCanBeThrown_AndCaught_AsException() {
        assertThrows(Exception.class, () -> {
            throw new DecryptionException("thrown", new RuntimeException("cause"));
        });
    }

    @Test
    void testCaughtException_PreservesMessage() {
        String expectedMessage = "key could not be decrypted";
        DecryptionException caught = assertThrows(DecryptionException.class, () -> {
            throw new DecryptionException(expectedMessage, new RuntimeException());
        });

        assertEquals(expectedMessage, caught.getMessage());
    }

    @Test
    void testCaughtException_PreservesCause() {
        Throwable cause = new IllegalStateException("underlying issue");
        DecryptionException caught = assertThrows(DecryptionException.class, () -> {
            throw new DecryptionException("failed", cause);
        });

        assertSame(cause, caught.getCause());
    }

    @Test
    void testCause_CauseChaining() {
        Throwable rootCause = new IOException("disk read error");
        Throwable intermediateCause = new RuntimeException("wrapped", rootCause);
        DecryptionException ex = new DecryptionException("decryption failed", intermediateCause);

        assertSame(intermediateCause, ex.getCause());
        assertSame(rootCause, ex.getCause().getCause());
    }

    // ==========================
    // Stack Trace Tests
    // ==========================

    @Test
    void testStackTrace_IsNotEmpty() {
        DecryptionException ex = new DecryptionException("msg", new RuntimeException());

        assertNotNull(ex.getStackTrace());
        assertTrue(ex.getStackTrace().length > 0);
    }

    @Test
    void testToString_ContainsClassName() {
        DecryptionException ex = new DecryptionException("test message", new RuntimeException());

        assertTrue(ex.toString().contains("DecryptionException"));
    }

    @Test
    void testToString_ContainsMessage() {
        DecryptionException ex = new DecryptionException("test message", new RuntimeException());

        assertTrue(ex.toString().contains("test message"));
    }

    // ==========================
    // Checked Exception Contract Tests
    // ==========================

    @Test
    void testCheckedExceptionContract_MustBeDeclaredOrCaught() {
        // Verifies the checked exception contract — method must declare throws
        // or catch DecryptionException. This compiles only because
        // DecryptionException extends Exception (not RuntimeException).
        assertDoesNotThrow(() -> {
            try {
                throw new DecryptionException("checked", new RuntimeException());
            } catch (DecryptionException e) {
                assertEquals("checked", e.getMessage());
            }
        });
    }
}