package com.eh.digitalpathology.ibex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "organ")
@RefreshScope
public class OrganMappingConfig {
    private Map<String, String> mapping;
    private List<String> allowed;

    public OrganMappingConfig(List<String> allowed) {
        this.allowed = allowed;
    }

    public Map<String, String> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public List<String> getAllowed() {
        return allowed;
    }

    public void setAllowed(List<String> allowed) {
        this.allowed = allowed;
    }
}
