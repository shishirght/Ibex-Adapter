package com.eh.digitalpathology.ibex.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class SignatureUtilsTest {

    private SignatureUtils signatureUtils;

    private static final String HMAC_SECRET = "test-secret-key";

    @BeforeEach
    void setUp() {
        signatureUtils = new SignatureUtils();
        ReflectionTestUtils.setField(signatureUtils, "hmacSecret", HMAC_SECRET);
    }

    // ==========================
    // signCanonical() Tests
    // ==========================

    @Test
    void testSignCanonical_ReturnsNonNullString() {
        String result = signatureUtils.signCanonical("some-canonical-string");
        assertNotNull(result);
    }

    @Test
    void testSignCanonical_ReturnsBase64UrlEncodedString() {
        String result = signatureUtils.signCanonical("some-canonical-string");
        // Base64 URL encoded without padding must not contain +, /, or =
        assertFalse(result.contains("+"));
        assertFalse(result.contains("/"));
        assertFalse(result.contains("="));
    }

    @Test
    void testSignCanonical_ReturnsSameOutput_ForSameInput() {
        String first = signatureUtils.signCanonical("canonical");
        String second = signatureUtils.signCanonical("canonical");
        assertEquals(first, second);
    }

    @Test
    void testSignCanonical_ReturnsDifferentOutput_ForDifferentInput() {
        String first = signatureUtils.signCanonical("canonical-one");
        String second = signatureUtils.signCanonical("canonical-two");
        assertNotEquals(first, second);
    }

    @Test
    void testSignCanonical_ReturnsDifferentOutput_ForDifferentSecret() {
        String resultWithSecret1 = signatureUtils.signCanonical("canonical");

        SignatureUtils otherUtils = new SignatureUtils();
        ReflectionTestUtils.setField(otherUtils, "hmacSecret", "different-secret");
        String resultWithSecret2 = otherUtils.signCanonical("canonical");

        assertNotEquals(resultWithSecret1, resultWithSecret2);
    }

    @Test
    void testSignCanonical_HandlesEmptyString() {
        String result = signatureUtils.signCanonical("");
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void testSignCanonical_HandlesSpecialCharacters() {
        String result = signatureUtils.signCanonical("path/to/object?query=value&other=123");
        assertNotNull(result);
    }

    @Test
    void testSignCanonical_ThrowsIllegalStateException_WhenSecretIsNull() {
        SignatureUtils brokenUtils = new SignatureUtils();
        ReflectionTestUtils.setField(brokenUtils, "hmacSecret", null);

        assertThrows(IllegalStateException.class,
                () -> brokenUtils.signCanonical("some-canonical"));
    }

    @Test
    void testSignCanonical_ProducesCorrectHmacSha256Length() {
        // HmacSHA256 produces 32 bytes → Base64 URL without padding = 43 chars
        String result = signatureUtils.signCanonical("test");
        byte[] decoded = Base64.getUrlDecoder().decode(result);
        assertEquals(32, decoded.length);
    }

    // ==========================
    // verify() Tests
    // ==========================

    @Test
    void testVerify_ReturnsTrue_WhenSignatureMatches() {
        String canonical = "GET\n/path\nbucket\nobject\n\n9999999999";
        String signature = signatureUtils.signCanonical(canonical);

        assertTrue(signatureUtils.verify(signature, canonical));
    }

    @Test
    void testVerify_ReturnsFalse_WhenSignatureDoesNotMatch() {
        String canonical = "GET\n/path\nbucket\nobject\n\n9999999999";
        String wrongSignature = signatureUtils.signCanonical("completely-different-canonical");

        assertFalse(signatureUtils.verify(wrongSignature, canonical));
    }

    @Test
    void testVerify_ReturnsFalse_WhenSignatureIsGarbage() {
        assertFalse(signatureUtils.verify("not-a-valid-signature!!!", "some-canonical"));
    }

    @Test
    void testVerify_ReturnsFalse_WhenSignatureIsEmpty() {
        assertFalse(signatureUtils.verify("", "some-canonical"));
    }

    @Test
    void testVerify_ReturnsFalse_WhenCanonicalTampered() {
        String canonical = "GET\n/path\nbucket\nobject\n\n9999999999";
        String signature = signatureUtils.signCanonical(canonical);
        String tamperedCanonical = "GET\n/path\nbucket\nobject\n\n1111111111";

        assertFalse(signatureUtils.verify(signature, tamperedCanonical));
    }

    @Test
    void testVerify_ReturnsFalse_WhenSignatureIsNull() {
        // Base64 decode of null throws exception — verify() catches it and returns false
        assertFalse(signatureUtils.verify(null, "some-canonical"));
    }

    @Test
    void testVerify_IsConsistent_AcrossMultipleCalls() {
        String canonical = "POST\n/upload\nbucket\nfile.svs\n\n9999999999";
        String signature = signatureUtils.signCanonical(canonical);

        assertTrue(signatureUtils.verify(signature, canonical));
        assertTrue(signatureUtils.verify(signature, canonical));
    }

    // ==========================
    // canonical() Tests
    // ==========================

    @Test
    void testCanonical_ReturnsCorrectFormat_WhenGenerationPresent() {
        String result = signatureUtils.canonical("GET", "/path", "my-bucket", "my-object", 42L, 1700000000L);
        assertEquals("GET\n/path\nmy-bucket\nmy-object\n42\n1700000000", result);
    }

    @Test
    void testCanonical_ReturnsEmptyGenerationField_WhenGenerationNull() {
        String result = signatureUtils.canonical("GET", "/path", "my-bucket", "my-object", null, 1700000000L);
        assertEquals("GET\n/path\nmy-bucket\nmy-object\n\n1700000000", result);
    }

    @Test
    void testCanonical_ReturnsCorrectFormat_ForPostMethod() {
        String result = signatureUtils.canonical("POST", "/upload", "bucket-x", "file.svs", null, 9999999999L);
        assertEquals("POST\n/upload\nbucket-x\nfile.svs\n\n9999999999", result);
    }

    @Test
    void testCanonical_ReturnsCorrectFormat_ForPutMethod() {
        String result = signatureUtils.canonical("PUT", "/update", "bucket-y", "slide.tiff", 99L, 8888888888L);
        assertEquals("PUT\n/update\nbucket-y\nslide.tiff\n99\n8888888888", result);
    }

    @Test
    void testCanonical_HandlesEmptyStrings() {
        String result = signatureUtils.canonical("", "", "", "", null, 0L);
        assertEquals("\n\n\n\n\n0", result);
    }

    @Test
    void testCanonical_GenerationZero_IsNotTreatedAsNull() {
        String result = signatureUtils.canonical("GET", "/path", "bucket", "object", 0L, 1000L);
        assertEquals("GET\n/path\nbucket\nobject\n0\n1000", result);
    }

    // ==========================
    // isExpired() Tests
    // ==========================

    @Test
    void testIsExpired_ReturnsFalse_WhenExpiryIsFarInFuture() {
        long futureEpoch = Instant.now().getEpochSecond() + 86400; // +1 day
        assertFalse(signatureUtils.isExpired(futureEpoch));
    }

    @Test
    void testIsExpired_ReturnsTrue_WhenExpiryIsInThePast() {
        long pastEpoch = Instant.now().getEpochSecond() - 86400; // -1 day
        assertTrue(signatureUtils.isExpired(pastEpoch));
    }

    @Test
    void testIsExpired_ReturnsTrue_WhenExpiryIsEpochZero() {
        assertTrue(signatureUtils.isExpired(0L));
    }

    @Test
    void testIsExpired_ReturnsTrue_WhenExpiryIsOneSecondAgo() {
        long oneSecondAgo = Instant.now().getEpochSecond() - 1;
        assertTrue(signatureUtils.isExpired(oneSecondAgo));
    }

    @Test
    void testIsExpired_ReturnsFalse_WhenExpiryIsOneHourFromNow() {
        long oneHourLater = Instant.now().getEpochSecond() + 3600;
        assertFalse(signatureUtils.isExpired(oneHourLater));
    }

    @Test
    void testIsExpired_ReturnsTrue_WhenExpiryIsLongAgo() {
        assertTrue(signatureUtils.isExpired(1000L)); // Jan 1970
    }
}