/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.nacos.config.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.client.config.impl.ConfigHttpClientManager;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;
import com.huaweicloud.nacos.config.NacosConfigConst;
import com.huaweicloud.nacos.config.manager.ConfigServiceManagerUtils;

public class NacosPropertiesFuzzyQueryService {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosPropertiesFuzzyQueryService.class);

  private static final String NACOS_CONFIG_QUERY_URI = "%s/nacos/v1/cs/configs";

  private static final NacosPropertiesFuzzyQueryService INSTANCE = new NacosPropertiesFuzzyQueryService();

  private NacosConfigProperties configProperties;

  private final Map<String, Properties> addressPropertiesMap = new HashMap<>();

  private NacosPropertiesFuzzyQueryService() {

  }

  public static NacosPropertiesFuzzyQueryService getInstance() {
    return INSTANCE;
  }

  public List<PropertyConfigItem> loadRouterProperties() {
    buildAddressPropertiesMap();
    List<String> addresses = getAddresses();
    for (String address : addresses) {
      try {
        NacosRestTemplate nacosRestTemplate = ConfigServiceManagerUtils.getNacosRestTemplate();
        String url = ConfigServiceManagerUtils.buildUrl(address, NACOS_CONFIG_QUERY_URI);
        Header header = ConfigServiceManagerUtils.initHeader(address, configProperties.getUsername(),
            addressPropertiesMap.get(address));
        HttpRestResult<PropertiePageQueryResult> response = nacosRestTemplate.get(url, header, buildQuery(),
            PropertiePageQueryResult.class);
        if (response.getData() != null && !CollectionUtils.isEmpty(response.getData().getPageItems())) {
          return response.getData().getPageItems();
        }
      } catch (Exception e) {
        LOGGER.error("load router properties failed!", e);
      }
    }
    return Collections.emptyList();
  }

  private Query buildQuery() {
    Query query = new Query();
    query.addParam("dataId", NacosConfigConst.LABEL_ROUTER_DATA_ID_PREFIX + "*");
    query.addParam("group", configProperties.getGroup());
    query.addParam("tenant", getNamespace());

    // this value is fixes when using blur query configs
    query.addParam("search", "blur");
    query.addParam("pageNo", 1);
    query.addParam("pageSize", 100);
    return query;
  }

  private String getNamespace() {
    return StringUtils.isEmpty(configProperties.getNamespace()) ? "" : configProperties.getNamespace();
  }

  private List<String> getAddresses() {
    List<String> addresses = new ArrayList<>();
    addresses.add(configProperties.getServerAddr());
    if (configProperties.isMasterStandbyEnabled() && !StringUtils.isEmpty(configProperties.getStandbyServerAddr())) {
      addresses.add(configProperties.getStandbyServerAddr());
    }
    return addresses;
  }

  private void buildAddressPropertiesMap() {
    Properties masterProperties = configProperties.assembleMasterNacosServerProperties();
    addressPropertiesMap.put(configProperties.getServerAddr(), masterProperties);
    if (!StringUtils.isEmpty(configProperties.getStandbyServerAddr())) {
      Properties standbyProperties = configProperties.assembleStandbyNacosServerProperties();
      addressPropertiesMap.put(configProperties.getStandbyServerAddr(), standbyProperties);
    }
  }

  public void setConfigProperties(NacosConfigProperties nacosConfigProperties) {
    if (configProperties == null) {
      this.configProperties = nacosConfigProperties;
    }
  }
}
