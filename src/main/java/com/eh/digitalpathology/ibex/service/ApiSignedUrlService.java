package com.eh.digitalpathology.ibex.service;

import com.eh.digitalpathology.ibex.util.SignatureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;

@RefreshScope
@Service
public class ApiSignedUrlService {

    private static Logger logger = LoggerFactory.getLogger( ApiSignedUrlService.class.getName( ) );

    private final SignatureUtils signatureUtils;

    @Value( "${ibex.callback-url}" )
    private String baseUrl;

    @Value("${signed.url.validity:30}")
    private long validityMinutes;

    public ApiSignedUrlService ( SignatureUtils signatureUtils ) {
        this.signatureUtils = signatureUtils;
    }

    public String generateApiSignedUrl ( String bucket, String object, Long generation ) {
        long expires = Instant.now( ).plusSeconds( validityMinutes * 60 ).getEpochSecond( );
        String path = "/api/files/download";
        String canonical = signatureUtils.canonical( "GET", path, bucket, object, generation, expires );
        String sig = signatureUtils.signCanonical( canonical );
        logger.info( "generateApiSignedUrl :: canonical :: {}", canonical );
        logger.info( "generateApiSignedUrl :: sig :: {} ", sig );

        String url =  UriComponentsBuilder.fromUriString( baseUrl + path ).queryParam( "bucket", bucket ).queryParam( "object", object ).queryParam( "generation", generation ).queryParam( "expires", expires ).queryParam( "sig", sig ).build( true ).toUriString( );
        logger.info( "generateApiSignedUrl :: url :: {} ", url );
        return url;
    }


}
