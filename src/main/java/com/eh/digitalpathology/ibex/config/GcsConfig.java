package com.eh.digitalpathology.ibex.config;

import com.eh.digitalpathology.ibex.util.CryptoUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Configuration
@RefreshScope
public class GcsConfig {

    @Value( "${project.name}" )
    private String projectId;

    @Value( "${service.account.name}" )
    private String clientEmail;

    @Value( "${project.secret}" )
    private String secretKey;

    @Value( "${signed.url.key}" )
    private String encryptedKey;

    @Lazy
    @Bean
    public Storage storage ( ) throws Exception {
        // Derive AES key from your secret
        String secretKeyBytes = CryptoUtils.encodeSecretKey( secretKey );
        SecretKeySpec secretKeySpec = new SecretKeySpec( secretKeyBytes.getBytes( StandardCharsets.UTF_8 ), "AES" );

        // Decrypt PKCS#8 private key (base64)
        String privateKeyPkcs8 = CryptoUtils.decrypt( encryptedKey, secretKeySpec );

        byte[] decodedKey = Base64.getDecoder( ).decode( privateKeyPkcs8 );
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec( decodedKey );
        KeyFactory keyFactory = KeyFactory.getInstance( "RSA" );
        PrivateKey privateKey = keyFactory.generatePrivate( keySpec );

        ServiceAccountCredentials credentials = ServiceAccountCredentials.newBuilder( ).setClientEmail( clientEmail ).setPrivateKey( privateKey ).build( );

        return StorageOptions.newBuilder( ).setProjectId( projectId ).setCredentials( credentials ).build( ).getService( );
    }
}