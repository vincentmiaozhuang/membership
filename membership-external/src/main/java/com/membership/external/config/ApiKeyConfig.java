package com.membership.external.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "api-keys")
public class ApiKeyConfig {

    private List<String> keys;

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public boolean isValidApiKey(String apiKey) {
        return keys != null && keys.contains(apiKey);
    }
}