/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;

import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.config.client.ConfigConstants;
import com.huaweicloud.config.client.ServiceCombConfigClient;

import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

/**
 * @Author wangqijun
 * @Date 11:06 2019-10-17
 **/
@Order(0)
public class ServiceCombPropertySourceLocator implements PropertySourceLocator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombPropertySourceLocator.class);

  private String project;

  private ServiceCombConfigProperties serviceCombConfigProperties;

  private ServiceCombConfigClient serviceCombConfigClient;

  public ServiceCombPropertySourceLocator(ServiceCombConfigProperties serviceCombConfigProperties,
      ServiceCombConfigClient serviceCombConfigClient, String project) {
    this.serviceCombConfigClient = serviceCombConfigClient;
    this.serviceCombConfigProperties = serviceCombConfigProperties;
    this.project = project;
  }

  @Override
  public PropertySource<?> locate(Environment environment) {
    if (StringUtils.isEmpty(serviceCombConfigProperties.getServerAddr())) {
      LOGGER.warn("Dynamic address is not configured, will not enable dynamic config.");
      return null;
    }

    ServiceCombConfigPropertySource serviceCombConfigPropertySource = new ServiceCombConfigPropertySource(
        ConfigConstants.PROPERTYSOURCE_NAME,
        serviceCombConfigClient);
    try {
      serviceCombConfigPropertySource
          .loadAllRemoteConfig(serviceCombConfigProperties, project);
    } catch (RemoteOperationException e) {
      LOGGER.error(e.getMessage(), e);
    }
    CompositePropertySource composite = new CompositePropertySource(ConfigConstants.PROPERTYSOURCE_NAME);
    composite.addPropertySource(serviceCombConfigPropertySource);
    return composite;
  }
}
