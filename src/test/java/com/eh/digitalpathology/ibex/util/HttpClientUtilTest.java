package com.eh.digitalpathology.ibex.util;

import com.eh.digitalpathology.ibex.exceptions.HealthcareApiException;
import com.eh.digitalpathology.ibex.model.HttpResponseWrapper;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HttpClientUtilTest {

    private CloseableHttpClient mockClient;
    private CloseableHttpResponse mockResponse;
    private StatusLine mockStatusLine;
    private HttpEntity mockEntity;

    @BeforeEach
    void setUp() throws Exception {
        mockClient = mock(CloseableHttpClient.class);
        mockResponse = mock(CloseableHttpResponse.class);
        mockStatusLine = mock(StatusLine.class);
        mockEntity = mock(HttpEntity.class);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(200);
    }

    private MockedStatic<RequestConfig> openRequestConfigMock() {
        RequestConfig.Builder rcBuilder = mock(RequestConfig.Builder.class, RETURNS_SELF);
        when(rcBuilder.build()).thenReturn(mock(RequestConfig.class));
        MockedStatic<RequestConfig> m = mockStatic(RequestConfig.class);
        m.when(RequestConfig::custom).thenReturn(rcBuilder);
        return m;
    }

    private MockedStatic<HttpClients> openHttpClientsMock() {
        HttpClientBuilder builder = mock(HttpClientBuilder.class);
        when(builder.setDefaultRequestConfig(any())).thenReturn(builder);
        when(builder.build()).thenReturn(mockClient);
        MockedStatic<HttpClients> m = mockStatic(HttpClients.class);
        m.when(HttpClients::custom).thenReturn(builder);
        return m;
    }

    @Test
    void sendGet_withEntity_returnsBodyAndStatus() throws Exception {
        try (
                MockedStatic<RequestConfig> rc = openRequestConfigMock();
                MockedStatic<HttpClients> hc = openHttpClientsMock();
                MockedStatic<EntityUtils> eu = mockStatic(EntityUtils.class)
        ) {
            when(mockClient.execute(any())).thenReturn(mockResponse);
            when(mockResponse.getEntity()).thenReturn(mockEntity);
            eu.when(() -> EntityUtils.toString(mockEntity)).thenReturn("get-body");

            HttpResponseWrapper result = HttpClientUtil.sendGet("http://host/get", Map.of("k", "v"));

            assertEquals(200, result.statusCode());
            assertEquals("get-body", result.body());
        }
    }

    @Test
    void sendGet_withNullEntity_returnsEmptyBody() throws Exception {
        try (
                MockedStatic<RequestConfig> rc = openRequestConfigMock();
                MockedStatic<HttpClients> hc = openHttpClientsMock()
        ) {
            when(mockClient.execute(any())).thenReturn(mockResponse);
            when(mockResponse.getEntity()).thenReturn(null);

            HttpResponseWrapper result = HttpClientUtil.sendGet("http://host/get", Map.of());

            assertEquals(200, result.statusCode());
            assertEquals("", result.body());
        }
    }

    @Test
    void sendGet_whenIOExceptionThrown_throwsWithIOMessage() throws Exception {
        try (
                MockedStatic<RequestConfig> rc = openRequestConfigMock();
                MockedStatic<HttpClients> hc = openHttpClientsMock()
        ) {
            when(mockClient.execute(any())).thenThrow(new IOException("io fail"));

            HealthcareApiException ex = assertThrows(HealthcareApiException.class,
                    () -> HttpClientUtil.sendGet("http://host/get", Map.of()));
            assertTrue(ex.getMessage().contains("IOException occurred while sending GET request"));
        }
    }

    @Test
    void sendGet_whenRuntimeExceptionThrown_throwsWithExceptionMessage() throws Exception {
        try (
                MockedStatic<RequestConfig> rc = openRequestConfigMock();
                MockedStatic<HttpClients> hc = openHttpClientsMock()
        ) {
            when(mockClient.execute(any())).thenThrow(new RuntimeException("runtime fail"));

            HealthcareApiException ex = assertThrows(HealthcareApiException.class,
                    () -> HttpClientUtil.sendGet("http://host/get", Map.of()));
            assertTrue(ex.getMessage().contains("Exception occurred while sending GET request"));
        }
    }

    @Test
    void sendPost_withEntity_returnsBodyAndStatus() throws Exception {
        try (
                MockedStatic<RequestConfig> rc = openRequestConfigMock();
                MockedStatic<HttpClients> hc = openHttpClientsMock();
                MockedStatic<EntityUtils> eu = mockStatic(EntityUtils.class)
        ) {
            when(mockClient.execute(any())).thenReturn(mockResponse);
            when(mockResponse.getEntity()).thenReturn(mockEntity);
            eu.when(() -> EntityUtils.toString(mockEntity)).thenReturn("post-body");

            HttpResponseWrapper result = HttpClientUtil.sendPost("http://host/post", "{}", Map.of("k", "v"));

            assertEquals(200, result.statusCode());
            assertEquals("post-body", result.body());
        }
    }

    @Test
    void sendPost_withNullEntity_returnsEmptyBody() throws Exception {
        try (
                MockedStatic<RequestConfig> rc = openRequestConfigMock();
                MockedStatic<HttpClients> hc = openHttpClientsMock()
        ) {
            when(mockClient.execute(any())).thenReturn(mockResponse);
            when(mockResponse.getEntity()).thenReturn(null);

            HttpResponseWrapper result = HttpClientUtil.sendPost("http://host/post", "{}", Map.of());

            assertEquals(200, result.statusCode());
            assertEquals("", result.body());
        }
    }

    @Test
    void sendPost_whenIOExceptionThrown_throwsWithIOMessage() throws Exception {
        try (
                MockedStatic<RequestConfig> rc = openRequestConfigMock();
                MockedStatic<HttpClients> hc = openHttpClientsMock()
        ) {
            when(mockClient.execute(any())).thenThrow(new IOException("io fail"));

            HealthcareApiException ex = assertThrows(HealthcareApiException.class,
                    () -> HttpClientUtil.sendPost("http://host/post", "{}", Map.of()));
            assertTrue(ex.getMessage().contains("IOException occurred while sending POST request"));
        }
    }

    @Test
    void sendPut_withEntity_returnsBodyAndStatus() throws Exception {
        try (
                MockedStatic<RequestConfig> rc = openRequestConfigMock();
                MockedStatic<HttpClients> hc = openHttpClientsMock();
                MockedStatic<EntityUtils> eu = mockStatic(EntityUtils.class)
        ) {
            when(mockClient.execute(any())).thenReturn(mockResponse);
            when(mockResponse.getEntity()).thenReturn(mockEntity);
            eu.when(() -> EntityUtils.toString(mockEntity)).thenReturn("put-body");

            HttpResponseWrapper result = HttpClientUtil.sendPut("http://host/put", "{}", Map.of("k", "v"));

            assertEquals(200, result.statusCode());
            assertEquals("put-body", result.body());
        }
    }

    @Test
    void sendPut_withNullEntity_returnsEmptyBody() throws Exception {
        try (
                MockedStatic<RequestConfig> rc = openRequestConfigMock();
                MockedStatic<HttpClients> hc = openHttpClientsMock()
        ) {
            when(mockClient.execute(any())).thenReturn(mockResponse);
            when(mockResponse.getEntity()).thenReturn(null);

            HttpResponseWrapper result = HttpClientUtil.sendPut("http://host/put", "{}", Map.of());

            assertEquals(200, result.statusCode());
            assertEquals("", result.body());
        }
    }

    @Test
    void sendPut_whenExceptionThrown_throwsHealthcareApiException() throws Exception {
        try (
                MockedStatic<RequestConfig> rc = openRequestConfigMock();
                MockedStatic<HttpClients> hc = openHttpClientsMock()
        ) {
            when(mockClient.execute(any())).thenThrow(new RuntimeException("put fail"));

            assertThrows(HealthcareApiException.class,
                    () -> HttpClientUtil.sendPut("http://host/put", "{}", Map.of()));
        }
    }

    @Test
    void getImageBytes_returnsBytes() throws Exception {
        byte[] imageBytes = {1, 2, 3, 4, 5};
        try (
                MockedStatic<RequestConfig> rc = openRequestConfigMock();
                MockedStatic<HttpClients> hc = openHttpClientsMock();
                MockedStatic<EntityUtils> eu = mockStatic(EntityUtils.class)
        ) {
            when(mockClient.execute(any())).thenReturn(mockResponse);
            when(mockResponse.getEntity()).thenReturn(mockEntity);
            eu.when(() -> EntityUtils.toByteArray(mockEntity)).thenReturn(imageBytes);

            byte[] result = HttpClientUtil.getImageBytes("http://host/image", Map.of("k", "v"));

            assertArrayEquals(imageBytes, result);
        }
    }

    @Test
    void getImageBytes_whenExceptionThrown_throwsHealthcareApiException() throws Exception {
        try (
                MockedStatic<RequestConfig> rc = openRequestConfigMock();
                MockedStatic<HttpClients> hc = openHttpClientsMock()
        ) {
            when(mockClient.execute(any())).thenThrow(new RuntimeException("image fail"));

            assertThrows(HealthcareApiException.class,
                    () -> HttpClientUtil.getImageBytes("http://host/image", Map.of()));
        }
    }
}