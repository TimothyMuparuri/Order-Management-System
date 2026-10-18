package com.oms.eventing.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan(basePackages = "com.oms.eventing")
@EntityScan(basePackages = "com.oms.eventing.idempotency")
@EnableJpaRepositories(basePackages = "com.oms.eventing.idempotency")
public class EventingAutoConfiguration {
}
