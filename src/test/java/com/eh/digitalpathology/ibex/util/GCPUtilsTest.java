package com.eh.digitalpathology.ibex.util;

import com.eh.digitalpathology.ibex.config.GcpConfig;
import com.eh.digitalpathology.ibex.exceptions.HealthcareApiException;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GCPUtilsTest {

    @Mock
    private GcpConfig gcpConfig;

    @Test
    void getAccessToken_whenCredsNull_returnsEmpty() throws HealthcareApiException {
        when(gcpConfig.getCreds()).thenReturn(null);
        assertEquals("", GCPUtils.getAccessToken(gcpConfig));
    }

    @Test
    void getAccessToken_whenCredsBlank_returnsEmpty() throws HealthcareApiException {
        when(gcpConfig.getCreds()).thenReturn("   ");
        assertEquals("", GCPUtils.getAccessToken(gcpConfig));
    }

    @Test
    void getAccessToken_whenCredsEmpty_returnsEmpty() throws HealthcareApiException {
        when(gcpConfig.getCreds()).thenReturn("");
        assertEquals("", GCPUtils.getAccessToken(gcpConfig));
    }

    @Test
    void getAccessToken_whenValidCreds_refreshesAndReturnsToken() throws Exception {
        when(gcpConfig.getCreds()).thenReturn("{\"type\":\"service_account\"}");

        GoogleCredentials mockScoped = mock(GoogleCredentials.class);
        GoogleCredentials mockBase = mock(GoogleCredentials.class);
        AccessToken mockToken = mock(AccessToken.class);

        when(mockBase.createScoped(any(java.util.Collection.class))).thenReturn(mockScoped);
        when(mockScoped.refreshAccessToken()).thenReturn(mockToken);
        when(mockToken.getTokenValue()).thenReturn("test-token-value");

        try (MockedStatic<GoogleCredentials> gcMock = mockStatic(GoogleCredentials.class)) {
            gcMock.when(() -> GoogleCredentials.fromStream(any(InputStream.class))).thenReturn(mockBase);

            String result = GCPUtils.getAccessToken(gcpConfig);

            assertEquals("test-token-value", result);
            verify(mockScoped).refreshIfExpired();
            verify(mockScoped).refreshAccessToken();
        }
    }

    @Test
    void getAccessToken_whenGoogleCredentialsThrows_throwsHealthcareApiException() {
        when(gcpConfig.getCreds()).thenReturn("invalid-creds");

        try (MockedStatic<GoogleCredentials> gcMock = mockStatic(GoogleCredentials.class)) {
            gcMock.when(() -> GoogleCredentials.fromStream(any(InputStream.class)))
                    .thenThrow(new RuntimeException("credentials parse failure"));

            HealthcareApiException ex = assertThrows(HealthcareApiException.class,
                    () -> GCPUtils.getAccessToken(gcpConfig));
            assertTrue(ex.getMessage().contains("Failed to get access token from service account credentials"));
            assertNotNull(ex.getCause());
        }
    }

    @Test
    void getAccessToken_whenRefreshThrows_throwsHealthcareApiException() throws Exception {
        when(gcpConfig.getCreds()).thenReturn("{\"type\":\"service_account\"}");

        GoogleCredentials mockScoped = mock(GoogleCredentials.class);
        GoogleCredentials mockBase = mock(GoogleCredentials.class);
        when(mockBase.createScoped(any(java.util.Collection.class))).thenReturn(mockScoped);
        doThrow(new RuntimeException("refresh failed")).when(mockScoped).refreshIfExpired();

        try (MockedStatic<GoogleCredentials> gcMock = mockStatic(GoogleCredentials.class)) {
            gcMock.when(() -> GoogleCredentials.fromStream(any(InputStream.class))).thenReturn(mockBase);

            HealthcareApiException ex = assertThrows(HealthcareApiException.class,
                    () -> GCPUtils.getAccessToken(gcpConfig));
            assertTrue(ex.getMessage().contains("Failed to get access token from service account credentials"));
        }
    }
}