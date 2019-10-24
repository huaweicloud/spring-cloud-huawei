package org.springframework.cloud.canary.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.canary.client.hytrix.CanaryHystrixConcurrencyStrategy;
import org.springframework.cloud.canary.client.rest.CanaryRestTemplateIntercptor;
import org.springframework.cloud.canary.client.track.CanaryHandlerInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
@Configuration
public class CanaryWebMvcConfigurer implements WebMvcConfigurer {

    @Bean
    public CanaryRestTemplateIntercptor canaryClientHttpRequestIntercptor(
            @Autowired(required = false) @LoadBalanced List<RestTemplate> restTemplates) {
        CanaryRestTemplateIntercptor intercptor = new CanaryRestTemplateIntercptor();
        if (restTemplates != null) {
            restTemplates.forEach(restTemplate -> restTemplate.getInterceptors().add(intercptor));
        }
        return intercptor;
    }

    @Bean
    public CanaryHystrixConcurrencyStrategy canaryHystrixConcurrencyStrategy() {
        return new CanaryHystrixConcurrencyStrategy();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CanaryHandlerInterceptor()).addPathPatterns("/**");
    }
}
