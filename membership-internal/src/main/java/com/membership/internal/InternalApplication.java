package com.membership.internal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "com.membership.internal",
    "com.membership.core",
    "com.membership.config",
    "com.membership.security",
    "com.membership.dto",
    "com.membership.controller",
    "com.membership.scheduler"
})
@EnableJpaRepositories(basePackages = "com.membership.core.repository")
@EntityScan(basePackages = "com.membership.core.entity")
@EnableScheduling
public class InternalApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternalApplication.class, args);
    }
}