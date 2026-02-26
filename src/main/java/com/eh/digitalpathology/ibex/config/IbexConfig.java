package com.eh.digitalpathology.ibex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ibex")
@RefreshScope
public class IbexConfig {

    private Api api;
    private String callbackUrl;

    public Api getApi ( ) {
        return api;
    }

    public void setApi ( Api api ) {
        this.api = api;
    }

    public String getCallbackUrl ( ) {
        return callbackUrl;
    }

    public void setCallbackUrl ( String callbackUrl ) {
        this.callbackUrl = callbackUrl;
    }

    public static class Api {
       private String url;
       private String key;

        public String getUrl ( ) {
            return url;
        }

        public void setUrl ( String url ) {
            this.url = url;
        }

        public String getKey ( ) {
            return key;
        }

        public void setKey ( String key ) {
            this.key = key;
        }
    }
}
