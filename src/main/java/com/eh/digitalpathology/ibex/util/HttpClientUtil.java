package com.eh.digitalpathology.ibex.util;

import com.eh.digitalpathology.ibex.exceptions.HealthcareApiException;
import com.eh.digitalpathology.ibex.model.HttpResponseWrapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class HttpClientUtil {

    private static final int TIMEOUT = 120000;
    private static final Logger logger = LoggerFactory.getLogger( HttpClientUtil.class );

    private HttpClientUtil ( ) {
    }

    private static CloseableHttpClient createClient ( ) {
        RequestConfig config = RequestConfig.custom( ).setConnectTimeout( TIMEOUT ).setSocketTimeout( TIMEOUT ).setConnectionRequestTimeout( TIMEOUT ).build( );
        return HttpClients.custom( ).setDefaultRequestConfig( config ).build( );
    }

    public static HttpResponseWrapper sendGet ( String url, Map< String, String > headers ) throws HealthcareApiException {
        try ( CloseableHttpClient client = createClient( ) ) {
            HttpGet request = new HttpGet( url );
            headers.forEach( request::setHeader );

            logger.info( "sendGet :: Sending GET request to URL: {} ", url );

            try ( CloseableHttpResponse response = client.execute( request ) ) {
                int statusCode = response.getStatusLine( ).getStatusCode( );
                HttpEntity entity = response.getEntity( );
                String responseBody = entity != null ? EntityUtils.toString( entity ) : "";

                return new HttpResponseWrapper( statusCode, responseBody );
            }
        } catch ( IOException e ) {
            throw new HealthcareApiException( "IOException occurred while sending GET request: " + e.getMessage( ), e );
        } catch ( Exception ex ) {
            throw new HealthcareApiException( "Exception occurred while sending GET request: " + ex.getMessage( ), ex );
        }
    }

    public static HttpResponseWrapper sendPost ( String url, String body, Map< String, String > headers ) throws HealthcareApiException {
        try ( CloseableHttpClient client = createClient( ) ) {
            HttpPost request = new HttpPost( url );
            headers.forEach( request::setHeader );
            request.setEntity( new StringEntity( body, ContentType.APPLICATION_JSON ) );

            try ( CloseableHttpResponse response = client.execute( request ) ) {
                int statusCode = response.getStatusLine( ).getStatusCode( );
                HttpEntity entity = response.getEntity( );
                String responseBody = entity != null ? EntityUtils.toString( entity ) : "";
                return new HttpResponseWrapper( statusCode, responseBody );
            }
        } catch ( IOException e ) {
            throw new HealthcareApiException( "IOException occurred while sending POST request: " + e.getMessage( ), e );
        }
    }

    public static HttpResponseWrapper sendPut ( String url, String body, Map< String, String > headers ) throws HealthcareApiException {
        try ( CloseableHttpClient client = createClient( ) ) {
            HttpPut request = new HttpPut( url );
            headers.forEach( request::setHeader );
            request.setEntity( new StringEntity( body, ContentType.APPLICATION_JSON ) );

            try ( CloseableHttpResponse response = client.execute( request ) ) {
                int statusCode = response.getStatusLine( ).getStatusCode( );
                HttpEntity entity = response.getEntity( );
                String responseBody = entity != null ? EntityUtils.toString( entity ) : "";
                return new HttpResponseWrapper( statusCode, responseBody );
            }
        } catch ( Exception ex ) {
            throw new HealthcareApiException( ex.getMessage( ) );
        }
    }

    public static byte[] getImageBytes ( String url, Map< String, String > headers ) throws HealthcareApiException {
        try ( CloseableHttpClient client = createClient( ) ) {
            HttpGet request = new HttpGet( url );
            headers.forEach( request::setHeader );
            try ( CloseableHttpResponse response = client.execute( request ) ) {
                return EntityUtils.toByteArray( response.getEntity( ) );
            }
        } catch ( Exception ex ) {
            throw new HealthcareApiException( ex.getMessage( ) );
        }
    }


}
