package com.eh.digitalpathology.ibex.util;

import com.eh.digitalpathology.ibex.config.GcpConfig;
import com.eh.digitalpathology.ibex.exceptions.HealthcareApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GCPUtilsTest {

    @Mock
    private GcpConfig gcpConfig;

    // ==========================
    // getAccessToken() Tests
    // ==========================

    @Test
    void testGetAccessToken_ReturnsEmpty_WhenCredsNull() throws HealthcareApiException {
        when(gcpConfig.getCreds()).thenReturn(null);

        String result = GCPUtils.getAccessToken(gcpConfig);

        assertEquals("", result);
        verify(gcpConfig, times(1)).getCreds();
    }

    @Test
    void testGetAccessToken_ReturnsEmpty_WhenCredsBlank() throws HealthcareApiException {
        when(gcpConfig.getCreds()).thenReturn("   ");

        String result = GCPUtils.getAccessToken(gcpConfig);

        assertEquals("", result);
        verify(gcpConfig, times(1)).getCreds();
    }

    @Test
    void testGetAccessToken_ReturnsEmpty_WhenCredsEmptyString() throws HealthcareApiException {
        when(gcpConfig.getCreds()).thenReturn("");

        String result = GCPUtils.getAccessToken(gcpConfig);

        assertEquals("", result);
        verify(gcpConfig, times(1)).getCreds();
    }

    @Test
    void testGetAccessToken_ThrowsHealthcareApiException_WhenCredsInvalid() {
        when(gcpConfig.getCreds()).thenReturn("not-valid-json-credentials");

        HealthcareApiException exception = assertThrows(HealthcareApiException.class,
                () -> GCPUtils.getAccessToken(gcpConfig));

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Failed to get access token from service account credentials"));
    }

    @Test
    void testGetAccessToken_ThrowsHealthcareApiException_WhenCredsMalformedJson() {
        when(gcpConfig.getCreds()).thenReturn("{malformed-json}");

        assertThrows(HealthcareApiException.class,
                () -> GCPUtils.getAccessToken(gcpConfig));
    }

    @Test
    void testGetAccessToken_ThrowsHealthcareApiException_WhenCredsValidJsonButInvalidServiceAccount() {
        // Valid JSON but not a valid service account key structure
        String invalidServiceAccountJson = "{\"type\":\"service_account\",\"project_id\":\"test\"}";
        when(gcpConfig.getCreds()).thenReturn(invalidServiceAccountJson);

        assertThrows(HealthcareApiException.class,
                () -> GCPUtils.getAccessToken(gcpConfig));
    }

    @Test
    void testGetAccessToken_WrapsOriginalCause_WhenExceptionThrown() {
        when(gcpConfig.getCreds()).thenReturn("invalid-creds");

        HealthcareApiException exception = assertThrows(HealthcareApiException.class,
                () -> GCPUtils.getAccessToken(gcpConfig));

        assertNotNull(exception.getCause());
    }
}