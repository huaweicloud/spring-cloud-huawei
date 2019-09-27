package org.springframework.cloud.dtm;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.huawei.paas.dtm.client.config.ConfigItems;

/**
 * @Author wangqijun
 * @Date 20:04 2019-09-09
 **/

@Configuration
@ConditionalOnProperty(value = "dtm", matchIfMissing = true)
@AutoConfigureAfter(name = {
    "org.springframework.cloud.servicecomb.discovery.registry.ServiceCombRegistryAutoConfiguration"})
@ComponentScan(basePackages = {"com.huawei.middleware.dtm.client",})
public class DtmClientConfiguration {

  @Bean
  public ConfigItems configItems() {
    return new ConfigItems();
  }
}

