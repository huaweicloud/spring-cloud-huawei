package org.springframework.cloud.canary.client;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.canary.client.ribbon.CanaryClientHttpRequestIntercptor;
import org.springframework.cloud.canary.client.ribbon.CanaryLoadBalanceRule;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class CanaryClientConfiguration {
    @Bean
    public IRule ribbonRule(
            @Autowired(required = false) IClientConfig config) {
        ZoneAvoidanceRule rule = new CanaryLoadBalanceRule();
        rule.initWithNiwsConfig(config);
        return rule;
    }

    @Bean
    public CanaryClientHttpRequestIntercptor grayClientHttpRequestIntercptor(
            @Autowired(required = false) @LoadBalanced List<RestTemplate> restTemplates) {
        CanaryClientHttpRequestIntercptor intercptor = new CanaryClientHttpRequestIntercptor();
        if (restTemplates != null) {
            restTemplates.forEach(restTemplate -> restTemplate.getInterceptors().add(intercptor));
        }
        return intercptor;
    }
}
