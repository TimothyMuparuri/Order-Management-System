package com.oms.intake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.oms.intake")
@EntityScan(basePackages = "com.oms.intake")
@EnableJpaRepositories(basePackages = "com.oms.intake")
public class IntakeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntakeServiceApplication.class, args);
    }
}