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

package com.huaweicloud.config.client;

import com.huaweicloud.common.transport.DealHeaderUtil;
import com.huaweicloud.common.transport.DefaultHttpTransport;
import com.huaweicloud.common.transport.ServiceCombAkSkProperties;
import com.huaweicloud.common.transport.ServiceCombRBACProperties;
import com.huaweicloud.common.transport.ServiceCombSSLProperties;
import com.huaweicloud.config.ServiceCombConfigProperties;

import org.springframework.util.StringUtils;

/**
 * @Author wangqijun
 * @Date 20:20 2019-10-19
 **/
public class ServiceCombConfigClientBuilder {

  private ServiceCombAkSkProperties serviceCombAkSkProperties;

  private ServiceCombRBACProperties serviceCombRBACProperties;

  private ServiceCombSSLProperties serviceCombSSLProperties;

  private ServiceCombConfigProperties serviceCombConfigProperties;

  public ServiceCombConfigClientBuilder setServiceCombAkSkProperties(
      ServiceCombAkSkProperties serviceCombAkSkProperties) {
    this.serviceCombAkSkProperties = serviceCombAkSkProperties;
    return this;
  }

  public ServiceCombConfigClientBuilder setServiceCombSSLProperties(
      ServiceCombSSLProperties serviceCombSSLProperties) {
    this.serviceCombSSLProperties = serviceCombSSLProperties;
    return this;
  }

  public ServiceCombConfigClientBuilder setServiceCombConfigProperties(
      ServiceCombConfigProperties serviceCombConfigProperties) {
    this.serviceCombConfigProperties = serviceCombConfigProperties;
    return this;
  }

  public ServiceCombConfigClientBuilder setServiceCombRBACProperties(
      ServiceCombRBACProperties serviceCombRBACProperties) {
    this.serviceCombRBACProperties = serviceCombRBACProperties;
    return this;
  }

  public ServiceCombConfigClient createServiceCombConfigClient() {
    DefaultHttpTransport httpTransport = new DefaultHttpTransport(serviceCombSSLProperties, serviceCombAkSkProperties,
        serviceCombRBACProperties, serviceCombConfigProperties.getDiscoveryAddress(),
        DealHeaderUtil.LONG_POLLING_SOCKET_TIMEOUT);
    if ("kie".equalsIgnoreCase(serviceCombConfigProperties.getServerType())) {
      return new KieClient(serviceCombConfigProperties.getServerAddr(), httpTransport);
    }
    return new ConfigCenterClient(serviceCombConfigProperties.getServerAddr(), httpTransport);
  }
}
