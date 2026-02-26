/**
 * SignedUrlCreationService is used to generate the signed url for downloading the study.
 * It fetches the encrypted key from runtime config and decrypt it using AES algorithm.
 * Author: Preeti Ankam
 * Date: November 15, 2024
 */

package com.eh.digitalpathology.ibex.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RefreshScope
@Service
public class SignedUrlCreationService {
    private static final Logger logger = LoggerFactory.getLogger( SignedUrlCreationService.class );

    @Value("${signed.url.validity:30}")
    private long urlValidity;

    private final Storage storage;

    public SignedUrlCreationService ( Storage storage ) {
        this.storage = storage;
    }

    public String generateSignedUrl ( String urlString ) throws MalformedURLException {
        try {
            URI uri = new URI( urlString );
            URL url = uri.toURL( );
            Pattern urlPattern = Pattern.compile( "/b/([^/]+)/o/([^?]+)" );
            Matcher urlMatcher = urlPattern.matcher( url.getPath( ) );

            if ( urlMatcher.find( ) ) {
                String bucketName = urlMatcher.group( 1 );
                String objectName = urlMatcher.group( 2 );
                BlobInfo blobInfo = BlobInfo.newBuilder( bucketName, objectName ).build( );
                URL signedUrl = storage.signUrl( blobInfo, urlValidity , TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature( ) );
                return signedUrl.toString( );
            }
        } catch ( IOException e ) {
            logger.error( "generateSignedUrl :: Error generating signed URL: {}", e.getMessage( ) );
        } catch ( Exception e ) {
            logger.error( "generateSignedUrl :: Unexpected error: ", e );
        }
        return urlString;
    }
}

