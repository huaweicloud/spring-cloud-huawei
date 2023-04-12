/*

 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.service.engine.common.configration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.UrlResource;

import com.huaweicloud.service.engine.common.configration.bootstrap.BootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ConfigBootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.DiscoveryBootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.InstanceProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.MicroserviceProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombAkSkProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombRBACProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombSSLProperties;

@Configuration
public class BootstrapPropertiesConfiguration {
  public static final String COMPONENT_YML_NAME = "component.yml";

  public static final String COMPONENT_YML_NAME_ALIAS = "component.yaml";

  public static final String COMPONENT_PROPERTY_SOURCE_NAME = "component-property-source";

  public static final String COMPONENT_ORDER_KEY = "x-component-order";

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.service")
  public MicroserviceProperties microserviceProperties() {
    return new MicroserviceProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.instance")
  public InstanceProperties instanceProperties() {
    return new InstanceProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.credentials")
  public ServiceCombAkSkProperties serviceCombAkSkProperties() {
    return new ServiceCombAkSkProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.credentials.account")
  public ServiceCombRBACProperties serviceCombRBACProperties() {
    return new ServiceCombRBACProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.ssl")
  public ServiceCombSSLProperties serviceCombSSLProperties() {
    return new ServiceCombSSLProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.discovery")
  public DiscoveryBootstrapProperties discoveryBootstrapProperties() {
    return new DiscoveryBootstrapProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.config")
  public ConfigBootstrapProperties configBootstrapProperties() {
    return new ConfigBootstrapProperties();
  }

  @Bean
  public BootstrapProperties bootstrapProperties(MicroserviceProperties microserviceProperties,
      InstanceProperties instanceProperties,
      DiscoveryBootstrapProperties discoveryBootstrapProperties,
      ConfigBootstrapProperties configBootstrapProperties,
      ServiceCombSSLProperties serviceCombSSLProperties,
      ServiceCombAkSkProperties serviceCombAkSkProperties,
      ServiceCombRBACProperties serviceCombRBACProperties) {
    return new BootstrapProperties(microserviceProperties, instanceProperties, discoveryBootstrapProperties,
        configBootstrapProperties, serviceCombSSLProperties, serviceCombAkSkProperties, serviceCombRBACProperties);
  }

  @Bean
  public PropertySourcesPlaceholderConfigurer componentYamlPropertySourcesPlaceholderConfigurer() {
    PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer() {
      @Override
      public void setEnvironment(Environment environment) {
        super.setEnvironment(environment);

        if (!(environment instanceof ConfigurableEnvironment)) {
          return;
        }

        ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
        configurableEnvironment.getPropertySources().addLast(componentPropertySource());
      }
    };
    return configurer;
  }

  private PropertySource<?> componentPropertySource() {
    try {
      List<Properties> result = new ArrayList<>();
      Enumeration<URL> urls = classLoader().getResources(COMPONENT_YML_NAME);
      urlToProperties(urls, result);
      urls = classLoader().getResources(COMPONENT_YML_NAME_ALIAS);
      urlToProperties(urls, result);

      result.sort(Comparator.comparingInt(this::getOrder));

      CompositePropertySource source = new CompositePropertySource(COMPONENT_PROPERTY_SOURCE_NAME);
      AtomicInteger counter = new AtomicInteger(0);
      result.forEach(item -> source.addPropertySource(new PropertiesPropertySource("component-" +
          counter.getAndIncrement() + "-(" + getOrder(item) + ")", item)));
      return source;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private ClassLoader classLoader() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      return BootstrapPropertiesConfiguration.class.getClassLoader();
    }
    return classLoader;
  }

  private void urlToProperties(Enumeration<URL> urls, List<Properties> properties) {
    while (urls.hasMoreElements()) {
      URL url = urls.nextElement();
      YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
      factoryBean.setResources(new UrlResource(url));
      properties.add(factoryBean.getObject());
    }
  }

  private int getOrder(Properties a) {
    String result = a.getProperty(COMPONENT_ORDER_KEY, "0");
    return Integer.parseInt(result);
  }
}
