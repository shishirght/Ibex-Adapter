package com.eh.digitalpathology.ibex.util;

import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class CryptoUtils {
    private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

    public static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    public static final int GCM_TAG_LENGTH = 16; // 16 bytes = 128-bit tag

    public static String decrypt( String encryptedData, SecretKey secretKey) throws DecryptionException {
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            String projectNumber = getProjectNumber();
            String ivSpec = encodeIVSpec(projectNumber);
            byte[] iv = Base64.getDecoder().decode(ivSpec);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new DecryptionException("Failed to decrypt data", ex);
        }
    }

    public static String encodeIVSpec(String projectNumber) {
        BigInteger projectNumberInt = new BigInteger(projectNumber);
        return String.format("%016x", projectNumberInt);
    }

    private static String getProjectNumber ( ) {
        String projectNumber = null;
        try ( ProjectsClient pc = ProjectsClient.create( ) ) {
            String projectName = "projects/prj-d-path-integration-cs1h";
            Project pr = pc.getProject( projectName );
            String[] a = pr.getName( ).split( "/" );
            projectNumber = a[ 1 ];
        } catch ( Exception e ) {
            logger.error( "getProjectNumber :: Exception while fetching project number {}", e.getMessage( ) );
        }
        return projectNumber;
    }

    public static String encodeSecretKey(String secretKey) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedHash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.substring(0, 32);
    }

    // Lightweight custom exception to mirror your original code
    public static class DecryptionException extends Exception {
        public DecryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
