package com.oms.allocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.oms.allocation")
@EntityScan(basePackages = "com.oms.allocation")
@EnableJpaRepositories(basePackages = "com.oms.allocation")
public class AllocationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AllocationServiceApplication.class, args);
    }
}