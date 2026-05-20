package com.membership.external.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "api.rate-limit")
public class RateLimitConfig {

    private int requestsPerMinute = 100;
    private int burstLimit = 200;

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public void setRequestsPerMinute(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public int getBurstLimit() {
        return burstLimit;
    }

    public void setBurstLimit(int burstLimit) {
        this.burstLimit = burstLimit;
    }
}