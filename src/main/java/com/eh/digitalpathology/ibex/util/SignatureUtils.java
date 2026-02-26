package com.eh.digitalpathology.ibex.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
@RefreshScope
public class SignatureUtils {

    @Value( "${app.hmac-secret}" )
    private String hmacSecret;

    public String signCanonical ( String canonical ) {
        try {
            Mac mac = Mac.getInstance( "HmacSHA256" );
            mac.init( new SecretKeySpec( hmacSecret.getBytes( StandardCharsets.UTF_8 ), "HmacSHA256" ) );
            byte[] raw = mac.doFinal( canonical.getBytes( StandardCharsets.UTF_8 ) );
            return Base64.getUrlEncoder( ).withoutPadding( ).encodeToString( raw );
        } catch ( Exception e ) {
            throw new IllegalStateException( "Failed to sign canonical string", e );
        }
    }

    public boolean verify ( String providedSig, String canonical ) {
        try {
            byte[] expectedSig = Base64.getUrlDecoder( ).decode( signCanonical( canonical ) );
            byte[] actualSig = Base64.getUrlDecoder( ).decode( providedSig );
            return java.security.MessageDigest.isEqual( expectedSig, actualSig );
        } catch ( Exception e ) {
            return false;
        }
    }

    public String canonical ( String method, String path, String bucket, String object, Long generation, long expiresEpochSec ) {
        return String.join( "\n", method, path, bucket, object, generation == null ? "" : generation.toString( ), String.valueOf( expiresEpochSec ) );
    }

    public boolean isExpired ( long expiresEpochSec ) {
        return Instant.now( ).getEpochSecond( ) > expiresEpochSec;
    }
}
