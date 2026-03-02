package com.eh.digitalpathology.ibex.model;

import com.eh.digitalpathology.ibex.util.CryptoUtils;
import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CryptoUtilsTest {


    @Test
    void encodeSecretKey_returnsTruncated32CharHexString() throws Exception {
        String result = CryptoUtils.encodeSecretKey("my-secret");
        assertNotNull(result);
        assertEquals(32, result.length());
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    void encodeSecretKey_iDeterministic_sameInputSameOutput() throws Exception {
        String first  = CryptoUtils.encodeSecretKey("same-input");
        String second = CryptoUtils.encodeSecretKey("same-input");
        assertEquals(first, second);
    }

    @Test
    void encodeSecretKey_differentInputsDifferentOutputs() throws Exception {
        String a = CryptoUtils.encodeSecretKey("input-a");
        String b = CryptoUtils.encodeSecretKey("input-b");
        assertNotEquals(a, b);
    }


    @Test
    void encodeIVSpec_returnsFormattedHexString() {
        String result = CryptoUtils.encodeIVSpec("123456789");
        assertNotNull(result);
        assertEquals(16, result.length());
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    void encodeIVSpec_zeroPadsShortNumbers() {
        String result = CryptoUtils.encodeIVSpec("1");
        assertEquals("0000000000000001", result);
    }


    @Test
    void decrypt_returnsDecryptedString_whenAllDependenciesSucceed() throws Exception {
        String projectNumber   = "123456789012";
        String ivHex           = CryptoUtils.encodeIVSpec(projectNumber);
        byte[] iv              = ivHex.getBytes(StandardCharsets.UTF_8); // mock returns this

        String plaintext       = "hello-world";
        byte[] plaintextBytes  = plaintext.getBytes(StandardCharsets.UTF_8);
        String encryptedBase64 = Base64.getEncoder().encodeToString("dummy-encrypted".getBytes());

        SecretKey secretKey = new SecretKeySpec("1234567890123456".getBytes(), "AES");

        try (
            MockedStatic<ProjectsClient> pcStaticMock = mockStatic(ProjectsClient.class);
            MockedConstruction<GCMParameterSpec> gcmMock = mockConstruction(GCMParameterSpec.class)
        ) {
            // Mock ProjectsClient.create() → returns a mock client
            ProjectsClient mockClient = mock(ProjectsClient.class, withSettings().extraInterfaces(AutoCloseable.class));
            Project mockProject = mock(Project.class);
            when(mockProject.getName()).thenReturn("projects/123456789012");
            when(mockClient.getProject(anyString())).thenReturn(mockProject);
            pcStaticMock.when(ProjectsClient::create).thenReturn(mockClient);

            // Mock Cipher to avoid real AES/GCM
            try (MockedStatic<Cipher> cipherStaticMock = mockStatic(Cipher.class)) {
                Cipher mockCipher = mock(Cipher.class);
                when(mockCipher.doFinal(any(byte[].class))).thenReturn(plaintextBytes);
                cipherStaticMock.when(() -> Cipher.getInstance(CryptoUtils.AES_GCM_NO_PADDING))
                        .thenReturn(mockCipher);

                // Mock Base64.getDecoder() for both IV decode and encryptedData decode
                try (MockedStatic<Base64> base64Mock = mockStatic(Base64.class)) {
                    Base64.Decoder mockDecoder = mock(Base64.Decoder.class);
                    when(mockDecoder.decode(anyString())).thenReturn(iv);
                    base64Mock.when(Base64::getDecoder).thenReturn(mockDecoder);

                    String result = CryptoUtils.decrypt(encryptedBase64, secretKey);
                    assertEquals(plaintext, result);
                }
            }
        }
    }


    @Test
    void decrypt_whenProjectsClientThrows_usesNullIvAndThrowsDecryptionException() {
        SecretKey secretKey = new SecretKeySpec("1234567890123456".getBytes(), "AES");

        try (MockedStatic<ProjectsClient> pcStaticMock = mockStatic(ProjectsClient.class)) {
            pcStaticMock.when(ProjectsClient::create).thenThrow(new RuntimeException("GCP unavailable"));

            // getProjectNumber returns null → encodeIVSpec(null) → NullPointerException inside decrypt → DecryptionException
            assertThrows(CryptoUtils.DecryptionException.class,
                    () -> CryptoUtils.decrypt("encryptedData", secretKey));
        }
    }

    // -------------------------------------------------------
    // decrypt — cipher.doFinal throws → DecryptionException
    // -------------------------------------------------------

    @Test
    void decrypt_whenCipherThrows_throwsDecryptionException() {
        SecretKey secretKey = new SecretKeySpec("1234567890123456".getBytes(), "AES");

        try (
            MockedStatic<ProjectsClient> pcStaticMock = mockStatic(ProjectsClient.class);
            MockedStatic<Cipher> cipherStaticMock = mockStatic(Cipher.class);
            MockedStatic<Base64> base64Mock = mockStatic(Base64.class);
            MockedConstruction<GCMParameterSpec> ignored = mockConstruction(GCMParameterSpec.class)
        ) {
            ProjectsClient mockClient = mock(ProjectsClient.class);
            Project mockProject = mock(Project.class);
            when(mockProject.getName()).thenReturn("projects/123456789012");
            when(mockClient.getProject(anyString())).thenReturn(mockProject);
            pcStaticMock.when(ProjectsClient::create).thenReturn(mockClient);

            Base64.Decoder mockDecoder = mock(Base64.Decoder.class);
            when(mockDecoder.decode(anyString())).thenReturn("0000000000000001".getBytes());
            base64Mock.when(Base64::getDecoder).thenReturn(mockDecoder);

            Cipher mockCipher = mock(Cipher.class);
            cipherStaticMock.when(() -> Cipher.getInstance(anyString())).thenReturn(mockCipher);
            try {
                when(mockCipher.doFinal(any(byte[].class))).thenThrow(new RuntimeException("bad decrypt"));
            } catch (Exception ignored2) {}

            assertThrows(CryptoUtils.DecryptionException.class,
                    () -> CryptoUtils.decrypt("encryptedData", secretKey));
        }
    }


    @Test
    void decryptionException_storesMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        CryptoUtils.DecryptionException ex = new CryptoUtils.DecryptionException("msg", cause);
        assertEquals("msg", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}