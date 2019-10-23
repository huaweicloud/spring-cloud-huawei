package org.springframework.cloud.canary.client;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.canary.client.feign.CanaryFeignConfiguration;
import org.springframework.cloud.canary.client.ribbon.CanaryClientConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * @Author GuoYl123
 * @Date 2019/10/11
 **/

@Configuration
@EnableConfigurationProperties
@ConditionalOnBean(SpringClientFactory.class)
@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = CanaryClientConfiguration.class)
@EnableFeignClients(defaultConfiguration = {CanaryFeignConfiguration.class})
public class CanaryClientAutoConfiguration {
}