package com.eh.digitalpathology.ibex.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignedUrlCreationServiceTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private SignedUrlCreationService signedUrlCreationService;

    @BeforeEach
    void setUp() {
        // Inject @Value field manually
        ReflectionTestUtils.setField(signedUrlCreationService, "urlValidity", 30L);
    }

    @Test
    void generateSignedUrl_shouldReturnSignedUrl_whenValidGcsUrl() throws Exception {
        // Arrange
        String inputUrl =
                "https://storage.googleapis.com/storage/v1/b/test-bucket/o/test-file.zip?alt=media";

        URL signedUrl = new URL("https://signed-url");

        when(storage.signUrl(
                any(BlobInfo.class),
                eq(30L),
                eq(TimeUnit.MINUTES),
                any(Storage.SignUrlOption.class)
        )).thenReturn(signedUrl);

        // Act
        String result = signedUrlCreationService.generateSignedUrl(inputUrl);

        // Assert
        assertEquals("https://signed-url", result);
        verify(storage).signUrl(any(BlobInfo.class), eq(30L), eq(TimeUnit.MINUTES), any());
    }

    @Test
    void generateSignedUrl_shouldReturnOriginalUrl_whenPatternDoesNotMatch() throws Exception {
        // Arrange
        String invalidUrl = "https://example.com/file.zip";

        // Act
        String result = signedUrlCreationService.generateSignedUrl(invalidUrl);

        // Assert
        assertEquals(invalidUrl, result);
        verifyNoInteractions(storage);
    }

    @Test
    @Disabled
    void generateSignedUrl_shouldReturnOriginalUrl_whenStorageThrowsIOException() throws Exception {
        // Arrange
        String inputUrl =
                "https://storage.googleapis.com/storage/v1/b/test-bucket/o/test-file.zip?alt=media";

        when(storage.signUrl(any(), anyLong(), any(), any()))
                .thenThrow(new IOException("GCS error"));

        // Act
        String result = signedUrlCreationService.generateSignedUrl(inputUrl);

        // Assert
        assertEquals(inputUrl, result);
    }

    @Test
    @Disabled
    void generateSignedUrl_shouldThrowMalformedURLException_whenUrlIsInvalid() {
        // Arrange
        String badUrl = "ht!tp://invalid-url";

        // Act & Assert
        assertThrows(
                MalformedURLException.class,
                () -> signedUrlCreationService.generateSignedUrl(badUrl)
        );
    }

    @Test
    void generateSignedUrl_shouldReturnOriginalUrl_whenUnexpectedExceptionOccurs() throws Exception {
        // Arrange
        String inputUrl =
                "https://storage.googleapis.com/storage/v1/b/test-bucket/o/test-file.zip?alt=media";

        when(storage.signUrl(any(), anyLong(), any(), any()))
                .thenThrow(new RuntimeException("Boom"));

        // Act
        String result = signedUrlCreationService.generateSignedUrl(inputUrl);

        // Assert
        assertEquals(inputUrl, result);
    }
}
