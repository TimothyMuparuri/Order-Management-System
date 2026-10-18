package com.oms.adminbff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.oms.adminbff")
@EntityScan(basePackages = "com.oms.adminbff")
@EnableJpaRepositories(basePackages = "com.oms.adminbff")
@EnableScheduling
public class AdminBffServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminBffServiceApplication.class, args);
    }
}