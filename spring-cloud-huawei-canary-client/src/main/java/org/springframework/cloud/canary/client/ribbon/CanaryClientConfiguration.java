package org.springframework.cloud.canary.client.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CanaryClientConfiguration {
    @Bean
    public IRule ribbonRule(
            @Autowired(required = false) IClientConfig config) {
        ZoneAvoidanceRule rule = new CanaryLoadBalanceRule();
        rule.initWithNiwsConfig(config);
        return rule;
    }

}
