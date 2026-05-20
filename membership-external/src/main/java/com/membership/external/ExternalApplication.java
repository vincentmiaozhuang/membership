package com.membership.external;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.membership.external",
    "com.membership.core"
})
@EnableJpaRepositories(basePackages = "com.membership.core.repository")
@EntityScan(basePackages = "com.membership.core.entity")
public class ExternalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExternalApplication.class, args);
    }
}