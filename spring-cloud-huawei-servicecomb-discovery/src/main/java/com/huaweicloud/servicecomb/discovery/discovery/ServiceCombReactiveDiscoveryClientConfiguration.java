package com.huaweicloud.servicecomb.discovery.discovery;

import com.huaweicloud.servicecomb.discovery.ConditionalOnServiceCombDiscoveryEnabled;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.cloud.client.ReactiveCommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author GuoYl123
 * @Date 2020/8/18
 **/
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnReactiveDiscoveryEnabled
@ConditionalOnServiceCombDiscoveryEnabled
@AutoConfigureAfter({ServiceCombDiscoveryClientConfiguration.class,
    ReactiveCompositeDiscoveryClientAutoConfiguration.class})
@AutoConfigureBefore({ReactiveCommonsClientAutoConfiguration.class})
public class ServiceCombReactiveDiscoveryClientConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ServiceCombReactiveDiscoveryClient serviceCombReactiveDiscoveryClient(
      ServiceCombDiscoveryProperties discoveryProperties, ServiceCombClient serviceCombClient) {
    return new ServiceCombReactiveDiscoveryClient(
        new ServiceCombDiscoveryClient(discoveryProperties, serviceCombClient));
  }
}
