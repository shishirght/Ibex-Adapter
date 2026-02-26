package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.util.SignatureUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiSignedUrlServiceTest {

    @Mock
    private SignatureUtils signatureUtils;

    @InjectMocks
    private ApiSignedUrlService apiSignedUrlService;

    @BeforeEach
    void setUp() {
        // Inject @Value fields
        ReflectionTestUtils.setField(apiSignedUrlService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(apiSignedUrlService, "validityMinutes", 30L);
    }

    @Test
    void generateApiSignedUrl_success() {
        // Arrange
        String bucket = "test-bucket";
        String object = "file.dcm";
        Long generation = 123L;

        String canonical = "canonical-string";
        String signature = "signed-value";

        when(signatureUtils.canonical(
                eq("GET"),
                eq("/api/files/download"),
                eq(bucket),
                eq(object),
                eq(generation),
                anyLong()
        )).thenReturn(canonical);

        when(signatureUtils.signCanonical(canonical))
                .thenReturn(signature);

        // Act
        String result = apiSignedUrlService.generateApiSignedUrl(bucket, object, generation);

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("http://localhost:8080/api/files/download"));
        assertTrue(result.contains("bucket=" + bucket));
        assertTrue(result.contains("object=" + object));
        assertTrue(result.contains("generation=" + generation));
        assertTrue(result.contains("sig=" + signature));
        assertTrue(result.contains("expires="));

        verify(signatureUtils, times(1))
                .canonical(eq("GET"), eq("/api/files/download"), eq(bucket), eq(object), eq(generation), anyLong());

        verify(signatureUtils, times(1))
                .signCanonical(canonical);
    }
}
