package com.huaweicloud.health.check;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class RegistryHealthIndicatorConfiguration {

    @Bean
    @Order(100)
    public RegistryHealthIndicator RegistryHealthIndicator() {
         return new RegistryHealthIndicator();
    }
}
