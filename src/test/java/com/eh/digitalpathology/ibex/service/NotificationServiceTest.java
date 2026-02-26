package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.config.GcpConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private IbexAdapterService ibexAdapterService;

    @Mock
    private ExecutorService executorService;

    @Mock
    private BarcodeStudyInfo barcodeStudyInfo;

    @Mock
    private GcpConfig gcpConfig;

    @Mock
    private BucketContext bucketContext;

    @Mock
    private Storage storage;

    @Mock
    private Blob blob;

    @InjectMocks
    private NotificationService notificationService;

    private String validJson;

    @BeforeEach
    void setUp() {
        validJson = """
                {
                  "name": "study123.zip",
                  "mediaLink": "http://media-link",
                  "bucket": "test-bucket"
                }
                """;
    }

    @Test
    void processNotifications_nullPayload() {
        ResponseEntity<String> response = notificationService.processNotifications(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Message payload is null or empty", response.getBody());
    }

    @Test
    void processNotifications_emptyPayload() {
        ResponseEntity<String> response = notificationService.processNotifications("");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Message payload is null or empty", response.getBody());
    }

    @Test
    void processNotifications_nonZipFile() {
        String payload = """
                {
                  "name": "file.txt",
                  "mediaLink": "link",
                  "bucket": "bucket"
                }
                """;

        ResponseEntity<String> response = notificationService.processNotifications(payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid file format", response.getBody());
    }

    @Test
    @Disabled
    void processNotifications_storageReturnsNull() throws Exception {
        mockStorage(null);

        ResponseEntity<String> response = notificationService.processNotifications(validJson);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No storage data is found", response.getBody());
    }

    @Test
    @Disabled
    void processNotifications_noMetadataFound() throws Exception {
        mockStorage(storage);
        when(storage.get("test-bucket", "study123.zip")).thenReturn(blob);
        when(blob.getMetadata()).thenReturn(null);

        ResponseEntity<String> response = notificationService.processNotifications(validJson);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No metadata found for object", response.getBody());
    }

    @Test
    @Disabled
    void processNotifications_success() throws Exception {
        mockStorage(storage);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("barcode", "BC123");
        metadata.put("seriesid", "SERIES1");
        metadata.put("dicomstoreurl", "DICOM_URL");

        when(storage.get("test-bucket", "study123.zip")).thenReturn(blob);
        when(blob.getMetadata()).thenReturn(metadata);
        when(executorService.submit(any(Runnable.class))).thenReturn(mock(Future.class));

        ResponseEntity<String> response = notificationService.processNotifications(validJson);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Processed", response.getBody());

        verify(bucketContext).setBucketName("test-bucket");
        verify(barcodeStudyInfo).put("BC123", "study123");
        verify(executorService).submit(any(Runnable.class));
    }

    @Test
    @Disabled
    void processNotifications_invalidJson() {
        ResponseEntity<String> response =
                notificationService.processNotifications("{invalid-json}");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Failed to parse JSON", response.getBody());
    }

    @Test
    @Disabled
    void processNotifications_unexpectedException() throws Exception {
        NotificationService spyService = spy(notificationService);
        doThrow(new RuntimeException("Boom"))
                .when(spyService).processNotifications(anyString());

        ResponseEntity<String> response =
                spyService.processNotifications(validJson);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    /**
     * Utility to mock static StorageOptions builder
     */
    private void mockStorage(Storage storageToReturn) throws Exception {
        when(gcpConfig.getCreds()).thenReturn("fake-creds");

        MockedStatic<StorageOptions> storageOptionsMock =
                mockStatic(StorageOptions.class);

        StorageOptions.Builder builder = mock(StorageOptions.Builder.class);
        StorageOptions options = mock(StorageOptions.class);

        storageOptionsMock.when(StorageOptions::newBuilder).thenReturn(builder);
        when(builder.setCredentials(any(ServiceAccountCredentials.class)))
                .thenReturn(builder);
        when(builder.build()).thenReturn(options);
        when(options.getService()).thenReturn(storageToReturn);
    }
}
