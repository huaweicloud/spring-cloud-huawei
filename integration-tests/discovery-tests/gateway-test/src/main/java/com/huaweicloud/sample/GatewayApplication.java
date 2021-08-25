package com.huaweicloud.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
  public static void main(String[] args) throws Exception {
    try {
      SpringApplication.run(GatewayApplication.class, args);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Bean
  public RouteLocator routeLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("order", r -> r.path("/**")
            .uri("lb://order"))
        .build();
  }

}
