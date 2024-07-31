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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.client.auth.impl.NacosAuthLoginConstant;
import com.alibaba.nacos.client.auth.impl.NacosClientAuthServiceImpl;
import com.alibaba.nacos.client.config.impl.ConfigHttpClientManager;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.plugin.auth.api.RequestResource;
import com.huaweicloud.nacos.config.NacosConfigConst;
import com.huaweicloud.nacos.config.manager.ConfigServiceManagerUtils;

public class NacosPropertySourceExtendLocator {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosPropertySourceExtendLocator.class);

  private static final String NACOS_CONFIG_QUERY_URI = "%s/nacos/v1/cs/configs";

  private final NacosConfigProperties configProperties;

  private Properties properties;

  private final Map<String, NacosClientAuthServiceImpl> address_auth_service = new ConcurrentHashMap<>();

  public NacosPropertySourceExtendLocator(NacosConfigProperties nacosConfigProperties) {
    this.configProperties = nacosConfigProperties;
  }

  public List<PropertyConfigItem> loadRouterProperties() {
    try {
      String address = chooseAddress();
      NacosRestTemplate nacosRestTemplate = ConfigHttpClientManager.getInstance().getNacosRestTemplate();
      HttpRestResult<PropertiePageQueryResult> response = nacosRestTemplate.get(buildUrl(address), initHeader(address),
          buildQuery(), PropertiePageQueryResult.class);
      return response.getData().getPageItems();
    } catch (Exception e) {
      LOGGER.error("load router properties failed!", e);
      return Collections.emptyList();
    }
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

  private Header initHeader(String address) {
    Header header = Header.newInstance();
    header.addParam(NacosAuthLoginConstant.ACCESSTOKEN, getAccessToken(address));
    return header;
  }

  private String buildUrl(String address) {
    String prefix = "";
    if (!configProperties.getServerAddr().startsWith("http")) {
      prefix = "http://";
    }
    return prefix + String.format(NACOS_CONFIG_QUERY_URI, address);
  }

  private String getNamespace() {
    return StringUtils.isEmpty(configProperties.getNamespace()) ? "" : configProperties.getNamespace();
  }

  private String chooseAddress() {
    properties = configProperties.assembleMasterNacosServerProperties();
    if (!configProperties.isMasterStandbyEnabled()) {
      return configProperties.getServerAddr();
    }
    if (ConfigServiceManagerUtils.checkServerConnect(configProperties.getServerAddr())) {
      return configProperties.getServerAddr();
    }
    if (ConfigServiceManagerUtils.checkServerConnect(configProperties.getStandbyServerAddr())) {
      properties = configProperties.assembleStandbyNacosServerProperties();
      return configProperties.getStandbyServerAddr();
    }
    return configProperties.getServerAddr();
  }

  private String getAccessToken(String address) {
    NacosClientAuthServiceImpl authService = address_auth_service.get(address);
    if (authService == null) {
      authService = new NacosClientAuthServiceImpl();
      authService.setServerList(Collections.singletonList(address));
      authService.setNacosRestTemplate(ConfigHttpClientManager.getInstance().getNacosRestTemplate());
      authService.login(properties);
      address_auth_service.put(address, authService);
    }
    return authService.getLoginIdentityContext(buildResource()).getParameter(NacosAuthLoginConstant.ACCESSTOKEN);
  }

  protected RequestResource buildResource() {
    String namespace = getNamespace();
    String group = configProperties.getGroup();
    String dataId = NacosConfigConst.LABEL_ROUTER_DATA_ID_PREFIX + "*";
    return RequestResource.configBuilder().setNamespace(namespace).setGroup(group).setResource(dataId).build();
  }
}
