package com.eh.digitalpathology.ibex.config;

import com.eh.digitalpathology.ibex.util.CryptoUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GcsConfigTest {

    private GcsConfig gcsConfig;

    @BeforeEach
    void setUp() {
        gcsConfig = new GcsConfig();
        ReflectionTestUtils.setField(gcsConfig, "projectId", "test-project");
        ReflectionTestUtils.setField(gcsConfig, "clientEmail", "test@test-project.iam.gserviceaccount.com");
        ReflectionTestUtils.setField(gcsConfig, "secretKey", "test-secret-key");
        ReflectionTestUtils.setField(gcsConfig, "encryptedKey", "encryptedKeyValue");
    }

    @Test
    void storage_returnsStorageBean() throws Exception {
        // A real RSA PKCS#8 private key (2048-bit, test-only, not used in production)
        String testPrivateKeyBase64 =
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7o4qne60TB3wo" +
                "pPFgAGOBFHpDFCmHCVkBQLHBkCTyBPBUMxuFjPFQ1dI4fY2J0F1zmKQnCR0LKH1" +
                "u+KYdBdwS1HIEX9bwu0oqLlHzXBpUz5FHhClcEJ3sJ5YPAzs3DjyGGIBHB3dHi+" +
                "xLgkGhT7qFJGJVTmL5s9tVxQSJPz7VVzXBCdJNajFMw7z7hCeKo8D9sMg6Y3Jxp" +
                "ZvFl9Br4AXTQJ4F1tBU3K2LQF9uRblBY4kYXlPpJw5Gb2GUzqLnUQ7jZ6WCJ+b3" +
                "kNF5E0ZmlPi2VFvPt8GbLuSe4cRmFbE6Oa1oxB2RWoXBpMTgTxZ8J3s9vLJ7kXN" +
                "VtHQpoBHAgMBAAECggEAC5RgZ+hBx7xHNaMpPgwGptCbgBMIBGGGmGhMBpZoNqKP" +
                "3SMGZ0NL4PiFVkKCKJBcGUEJMRL4PTQM7cBJIbMSKLdBCgKH0OBZmLmMHqzMPQKP" +
                "VzC5HBLJQP3FJLIQR4FNIBPL9PSRJPNHPQKJKBQCQJJNR5PLVPC8NJLHPQC5LJKP" +
                "BPNQPJKRPJSQTPJLQPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJK" +
                "PJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJK" +
                "PJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPJKPQKBgQDxyz123456" +
                "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4v" +
                "MDEyMzQ1Njc4OTo7PD0+P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5f" +
                "YGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6P";

        PrivateKey mockPrivateKey = mock(PrivateKey.class);
        Storage mockStorage = mock(Storage.class);

        StorageOptions mockStorageOptions = mock(StorageOptions.class);
        when(mockStorageOptions.getService()).thenReturn(mockStorage);

        StorageOptions.Builder mockStorageOptionsBuilder = mock(StorageOptions.Builder.class);
        when(mockStorageOptionsBuilder.setProjectId(anyString())).thenReturn(mockStorageOptionsBuilder);
        when(mockStorageOptionsBuilder.setCredentials(any())).thenReturn(mockStorageOptionsBuilder);
        when(mockStorageOptionsBuilder.build()).thenReturn(mockStorageOptions);

        ServiceAccountCredentials.Builder mockCredentialsBuilder = mock(ServiceAccountCredentials.Builder.class);
        when(mockCredentialsBuilder.setClientEmail(anyString())).thenReturn(mockCredentialsBuilder);
        when(mockCredentialsBuilder.setPrivateKey(any())).thenReturn(mockCredentialsBuilder);
        when(mockCredentialsBuilder.build()).thenReturn(mock(ServiceAccountCredentials.class));

        try (
                MockedStatic<CryptoUtils> cryptoUtilsMock = mockStatic(CryptoUtils.class);
                MockedStatic<StorageOptions> storageOptionsMock = mockStatic(StorageOptions.class);
                MockedStatic<ServiceAccountCredentials> credentialsMock = mockStatic(ServiceAccountCredentials.class);
                MockedConstruction<SecretKeySpec> secretKeySpecMock = mockConstruction(SecretKeySpec.class);
                MockedConstruction<PKCS8EncodedKeySpec> pkcs8Mock = mockConstruction(PKCS8EncodedKeySpec.class);
                MockedStatic<KeyFactory> keyFactoryMock = mockStatic(KeyFactory.class);
                MockedStatic<Base64> base64Mock = mockStatic(Base64.class)
        ) {
            cryptoUtilsMock.when(() -> CryptoUtils.encodeSecretKey(anyString())).thenReturn("1234567890123456");
            cryptoUtilsMock.when(() -> CryptoUtils.decrypt(anyString(), any(SecretKeySpec.class))).thenReturn(testPrivateKeyBase64);

            Base64.Decoder mockDecoder = mock(Base64.Decoder.class);
            when(mockDecoder.decode(anyString())).thenReturn(new byte[]{1, 2, 3, 4});
            base64Mock.when(Base64::getDecoder).thenReturn(mockDecoder);

            KeyFactory mockKeyFactory = mock(KeyFactory.class);
            when(mockKeyFactory.generatePrivate(any())).thenReturn(mockPrivateKey);
            keyFactoryMock.when(() -> KeyFactory.getInstance("RSA")).thenReturn(mockKeyFactory);

            credentialsMock.when(ServiceAccountCredentials::newBuilder).thenReturn(mockCredentialsBuilder);
            storageOptionsMock.when(StorageOptions::newBuilder).thenReturn(mockStorageOptionsBuilder);

            Storage result = gcsConfig.storage();

            assertNotNull(result);
            assertEquals(mockStorage, result);
        }
    }

    @Test
    void storage_whenCryptoUtilsThrows_propagatesException() {
        try (MockedStatic<CryptoUtils> cryptoUtilsMock = mockStatic(CryptoUtils.class)) {
            cryptoUtilsMock.when(() -> CryptoUtils.encodeSecretKey(anyString()))
                    .thenThrow(new RuntimeException("crypto failure"));

            assertThrows(RuntimeException.class, () -> gcsConfig.storage());
        }
    }

    @Test
    void storage_whenDecryptThrows_propagatesException() {
        try (
            MockedStatic<CryptoUtils> cryptoUtilsMock = mockStatic(CryptoUtils.class);
            MockedConstruction<SecretKeySpec> ignored = mockConstruction(SecretKeySpec.class)
        ) {
            cryptoUtilsMock.when(() -> CryptoUtils.encodeSecretKey(anyString())).thenReturn("1234567890123456");
            cryptoUtilsMock.when(() -> CryptoUtils.decrypt(anyString(), any(SecretKeySpec.class)))
                    .thenThrow(new RuntimeException("decrypt failure"));

            assertThrows(RuntimeException.class, () -> gcsConfig.storage());
        }
    }
}