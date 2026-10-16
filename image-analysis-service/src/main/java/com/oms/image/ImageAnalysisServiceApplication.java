package com.oms.image;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.oms.image")
@EntityScan(basePackages = "com.oms.image")
@EnableJpaRepositories(basePackages = "com.oms.image")
public class ImageAnalysisServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageAnalysisServiceApplication.class, args);
    }
}