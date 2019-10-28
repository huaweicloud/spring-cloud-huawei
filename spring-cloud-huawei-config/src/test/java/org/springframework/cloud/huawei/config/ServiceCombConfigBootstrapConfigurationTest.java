package org.springframework.cloud.huawei.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * @Author wangqijun
 * @Date 20:05 2019-10-27
 **/
public class ServiceCombConfigBootstrapConfigurationTest {
  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

  @Test
  public void serviceCombPropertySourceLocator() {
    this.contextRunner.withUserConfiguration(ServiceCombConfigBootstrapConfiguration.class)
        .run(context -> {
          ServiceCombConfigProperties serviceCombConfigProperties = context.getBean(ServiceCombConfigProperties.class);
          assertThat(serviceCombConfigProperties.isEnable())
              .isEqualTo(true);
        });
  }
}