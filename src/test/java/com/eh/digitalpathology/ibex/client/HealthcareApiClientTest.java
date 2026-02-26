package com.eh.digitalpathology.ibex.client;

import com.eh.digitalpathology.ibex.config.GcpConfig;
import com.eh.digitalpathology.ibex.exceptions.HealthcareApiException;
import com.eh.digitalpathology.ibex.model.HttpResponseWrapper;
import com.eh.digitalpathology.ibex.util.GCPUtils;
import com.eh.digitalpathology.ibex.util.HttpClientUtil;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthcareApiClientTest {

    @Mock
    private GcpConfig gcpConfig;

    private HealthcareApiClient healthcareApiClient;

    private MockedStatic<GCPUtils> gcpUtilsMock;
    private MockedStatic<HttpClientUtil> httpClientUtilMock;

    @BeforeEach
    void setUp() {
        healthcareApiClient = new HealthcareApiClient(gcpConfig);
        gcpUtilsMock = Mockito.mockStatic(GCPUtils.class);
        httpClientUtilMock = Mockito.mockStatic(HttpClientUtil.class);
    }

    @AfterEach
    void tearDown() {
        gcpUtilsMock.close();
        httpClientUtilMock.close();
    }

    /**
     * SUCCESS CASE
     * HTTP 200 → metadata returned
     */
    @Test
    void fetchMetadata_success_returnsResponseBody() throws Exception {
        // Arrange
        when(gcpConfig.getDicomWebUrl())
                .thenReturn("https://healthcare.googleapis.com");

        gcpUtilsMock.when(() -> GCPUtils.getAccessToken(gcpConfig))
                .thenReturn("mock-token");

        HttpResponseWrapper response =
                new HttpResponseWrapper(HttpStatus.SC_OK, "{ \"meta\": \"data\" }");

        httpClientUtilMock.when(() ->
                        HttpClientUtil.sendGet(anyString(), anyMap()))
                .thenReturn(response);

        // Act
        String result = healthcareApiClient.fetchMetadata(
                "study123", "series456", "store789");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("meta"));

        httpClientUtilMock.verify(() ->
                HttpClientUtil.sendGet(
                        contains("/studies/study123/series/series456/metadata"),
                        argThat(headers ->
                                headers.containsKey("Authorization")
                                        && headers.containsKey("Accept")
                        )
                )
        );
    }

    /**
     * FAILURE CASE
     * HTTP != 200 → HealthcareApiException
     */
    @Test
    @Disabled
    void fetchMetadata_non200Status_throwsHealthcareApiException() {
        // Arrange
        when(gcpConfig.getDicomWebUrl())
                .thenReturn("https://healthcare.googleapis.com");

        gcpUtilsMock.when(() -> GCPUtils.getAccessToken(gcpConfig))
                .thenReturn("mock-token");

        HttpResponseWrapper response =
                new HttpResponseWrapper(HttpStatus.SC_BAD_REQUEST, "Bad Request");

        httpClientUtilMock.when(() ->
                        HttpClientUtil.sendGet(anyString(), anyMap()))
                .thenReturn(response);

        // Act + Assert
        HealthcareApiException ex = assertThrows(
                HealthcareApiException.class,
                () -> healthcareApiClient.fetchMetadata(
                        "study1", "series1", "store1")
        );

        assertTrue(ex.getMessage().contains("Failed to fetch metadata"));
    }

    /**
     * EXCEPTION CASE
     * HttpClientUtil throws runtime exception
     */
    @Test
    void fetchMetadata_httpClientThrowsException_wrappedInHealthcareApiException() {
        // Arrange
        when(gcpConfig.getDicomWebUrl())
                .thenReturn("https://healthcare.googleapis.com");

        gcpUtilsMock.when(() -> GCPUtils.getAccessToken(gcpConfig))
                .thenReturn("mock-token");

        httpClientUtilMock.when(() ->
                        HttpClientUtil.sendGet(anyString(), anyMap()))
                .thenThrow(new RuntimeException("Connection timeout"));

        // Act + Assert
        HealthcareApiException ex = assertThrows(
                HealthcareApiException.class,
                () -> healthcareApiClient.fetchMetadata(
                        "studyX", "seriesY", "storeZ")
        );

        assertTrue(ex.getMessage()
                .contains("Error occurred while fetching metadata"));
        assertNotNull(ex.getCause());
    }

    /**
     * EDGE CASE
     * Access token = null → Authorization header NOT added
     */
    @Test
    void fetchMetadata_nullAccessToken_noAuthorizationHeader() throws Exception {
        // Arrange
        when(gcpConfig.getDicomWebUrl())
                .thenReturn("https://healthcare.googleapis.com");

        gcpUtilsMock.when(() -> GCPUtils.getAccessToken(gcpConfig))
                .thenReturn(null);

        HttpResponseWrapper response =
                new HttpResponseWrapper(HttpStatus.SC_OK, "{}");

        httpClientUtilMock.when(() ->
                        HttpClientUtil.sendGet(anyString(), anyMap()))
                .thenReturn(response);

        // Act
        healthcareApiClient.fetchMetadata("s1", "s2", "s3");

        // Assert
        httpClientUtilMock.verify(() ->
                HttpClientUtil.sendGet(
                        anyString(),
                        argThat(headers ->
                                !headers.containsKey("Authorization")
                                        && headers.containsKey("Accept")
                        )
                )
        );
    }
}
