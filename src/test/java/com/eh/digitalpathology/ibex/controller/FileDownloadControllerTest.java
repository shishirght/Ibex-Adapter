package com.eh.digitalpathology.ibex.controller;

import com.eh.digitalpathology.ibex.util.SignatureUtils;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FileDownloadControllerTest {

    @Mock
    private Storage storage;

    @Mock
    private SignatureUtils signatureUtils;

    @Mock
    private Blob blob;

    @Mock
    private ReadChannel readChannel;

    @InjectMocks
    private FileDownloadController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void download_shouldReturn410_whenLinkExpired() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(signatureUtils.canonical(any(), any(), any(), any(), any(), anyLong()))
                .thenReturn("canonical");
        when(signatureUtils.isExpired(anyLong())).thenReturn(true);

        controller.download(
                "bucket",
                "file.zip",
                null,
                123L,
                "sig",
                response
        );

        assertEquals(HttpServletResponse.SC_GONE, response.getStatus());
        assertEquals("Link expired", response.getContentAsString());
    }

    @Test
    void download_shouldReturn403_whenSignatureInvalid() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(signatureUtils.canonical(any(), any(), any(), any(), any(), anyLong()))
                .thenReturn("canonical");
        when(signatureUtils.isExpired(anyLong())).thenReturn(false);
        when(signatureUtils.verify(anyString(), anyString())).thenReturn(false);

        controller.download(
                "bucket",
                "file.zip",
                null,
                123L,
                "bad-sig",
                response
        );

        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals("Invalid signature", response.getContentAsString());
    }

    @Test
    void download_shouldReturn404_whenBlobNotFound() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(signatureUtils.canonical(any(), any(), any(), any(), any(), anyLong()))
                .thenReturn("canonical");
        when(signatureUtils.isExpired(anyLong())).thenReturn(false);
        when(signatureUtils.verify(anyString(), anyString())).thenReturn(true);
        when(storage.get(any(BlobId.class))).thenReturn(null);

        controller.download(
                "bucket",
                "file.zip",
                null,
                123L,
                "sig",
                response
        );

        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        assertEquals("Object not found", response.getContentAsString());
    }

    @Test
    void download_shouldStreamFileSuccessfully() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(signatureUtils.canonical(any(), any(), any(), any(), any(), anyLong()))
                .thenReturn("canonical");
        when(signatureUtils.isExpired(anyLong())).thenReturn(false);
        when(signatureUtils.verify(anyString(), anyString())).thenReturn(true);

        when(storage.get(any(BlobId.class))).thenReturn(blob);
        when(blob.getName()).thenReturn("folder/file.zip");
        when(blob.getSize()).thenReturn(5L);
        when(blob.getEtag()).thenReturn("etag");
        when(blob.reader()).thenReturn(readChannel);

        // Simulate one read then EOF
        when(readChannel.read(any(ByteBuffer.class)))
                .thenAnswer(invocation -> {
                    ByteBuffer buffer = invocation.getArgument(0);
                    buffer.put("hello".getBytes());
                    return 5;
                })
                .thenReturn(-1);

        controller.download(
                "bucket",
                "file.zip",
                null,
                123L,
                "sig",
                response
        );

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/zip", response.getContentType());
        assertEquals("hello", response.getContentAsString());
    }

    @Test
    void download_shouldReturn500_onUnexpectedException() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(signatureUtils.canonical(any(), any(), any(), any(), any(), anyLong()))
                .thenThrow(new RuntimeException("boom"));

        controller.download(
                "bucket",
                "file.zip",
                null,
                123L,
                "sig",
                response
        );

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getContentAsString().contains("Server error"));
    }
}
