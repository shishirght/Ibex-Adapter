package com.eh.digitalpathology.ibex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gcp-config")
@RefreshScope
public class GcpConfig {
    private String creds;
    private String dicomWebUrl;

    public String getDicomWebUrl() {
        return dicomWebUrl;
    }

    public void setDicomWebUrl(String dicomWebUrl) {
        this.dicomWebUrl = dicomWebUrl;
    }

    public String getCreds() {
        return creds;
    }

    public void setCreds(String creds) {
        this.creds = creds;
    }
}