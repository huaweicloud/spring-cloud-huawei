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
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.shaded.com.google.gson.Gson;
import com.alibaba.nacos.shaded.com.google.gson.reflect.TypeToken;
import com.huaweicloud.nacos.config.NacosConfigConst;
import com.huaweicloud.nacos.config.manager.ConfigServiceManagerUtils;

public class NacosPropertySourceExtendLocator {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosPropertySourceExtendLocator.class);

  private static final String NACOS_CONFIG_QUERY_URI
      = "%s/nacos/v1/cs/configs?dataId=%s*&group=%s&pageNo=1&pageSize=1000&tenant=%s&search=blur";

  private final TypeToken<List<PropertyConfigItem>> typeToken = new TypeToken<List<PropertyConfigItem>>() {};

  private final NacosConfigProperties properties;

  public NacosPropertySourceExtendLocator(NacosConfigProperties nacosConfigProperties) {
    this.properties = nacosConfigProperties;
  }

  public List<PropertyConfigItem> loadRouterProperties() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      String response = restTemplate.exchange(buildUrl(), HttpMethod.GET,
          new HttpEntity<>(initHeaders()), String.class).getBody();
      JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response));
      JSONArray jsonArray = jsonObject.getJSONArray("pageItems");
      Gson gson = new Gson();
      return gson.fromJson(jsonArray.toString(), typeToken.getType());
    } catch (Exception e) {
      LOGGER.error("load router properties failed!", e);
      return Collections.emptyList();
    }
  }

  private String buildUrl() {
    String prefix = "";
    if (!properties.getServerAddr().startsWith("http")) {
      prefix = "http://";
    }
    String namespace = StringUtils.isEmpty(properties.getNamespace()) ? "" : properties.getNamespace();
    return prefix + String.format(NACOS_CONFIG_QUERY_URI, chooseAddress(), NacosConfigConst.LABEL_ROUTER_DATA_ID_PREFIX,
        properties.getGroup(), namespace);
  }

  private String chooseAddress() {
    if (!properties.isMasterStandbyEnabled()) {
      return properties.getServerAddr();
    }
    if (ConfigServiceManagerUtils.checkServerConnect(properties.getServerAddr())) {
      return properties.getServerAddr();
    }
    if (ConfigServiceManagerUtils.checkServerConnect(properties.getStandbyServerAddr())) {
      return properties.getStandbyServerAddr();
    }
    return properties.getServerAddr();
  }

  private HttpHeaders initHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (!StringUtils.isEmpty(properties.getUsername())) {
      headers.set("username", properties.getUsername());
    }
    if (!StringUtils.isEmpty(properties.getUsername())) {
      headers.set("password", properties.getPassword());
    }
    if (!StringUtils.isEmpty(properties.getAccessKey())) {
      headers.set("accessKey", properties.getAccessKey());
    }
    if (!StringUtils.isEmpty(properties.getSecretKey())) {
      headers.set("secretKey", properties.getSecretKey());
    }
    return headers;
  }
}
