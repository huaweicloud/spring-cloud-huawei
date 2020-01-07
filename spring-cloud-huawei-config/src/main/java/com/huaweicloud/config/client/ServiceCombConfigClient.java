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

import com.fasterxml.jackson.core.type.TypeReference;
import com.huaweicloud.common.transport.URLConfig;
import com.huaweicloud.common.util.URLUtil;
import com.huaweicloud.config.ServiceCombConfigProperties;
import com.huaweicloud.config.kie.KVResponse;
import com.huaweicloud.config.kie.KieUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Map.Entry;
import org.apache.http.HttpStatus;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.exception.RemoteServerUnavailableException;
import com.huaweicloud.common.transport.HttpTransport;
import com.huaweicloud.common.transport.Response;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @Author wangqijun
 * @Date 11:09 2019-10-17
 **/
public class ServiceCombConfigClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombConfigClient.class);

  private HttpTransport httpTransport;

  URLConfig configCenterConfig = new URLConfig();

  public ServiceCombConfigClient(String urls, HttpTransport httpTransport) {
    this.httpTransport = httpTransport;
    configCenterConfig.addUrl(URLUtil.getEnvConfigUrl());
    if (configCenterConfig.isEmpty()) {
      configCenterConfig.addUrl(URLUtil.dealMutiUrl(urls));
    }
  }

  /**
   * load all remote config from config center
   *
   * @return
   * @throws RemoteOperationException
   */
  public Map<String, String> loadAll(ServiceCombConfigProperties serviceCombConfigProperties,
      String project) throws RemoteOperationException {
    project = project != null && !project.isEmpty() ? project : ConfigConstants.DEFAULT_PROJECT;
    if (!StringUtils.isEmpty(serviceCombConfigProperties.getServerType())
        && serviceCombConfigProperties.getServerType().equals("kie")) {
      return loadFromKie(serviceCombConfigProperties, project);
    }
    return loadFromConfigCenter(QueryParamUtil.spliceDimensionsInfo(serviceCombConfigProperties),
        project);
  }

  /**
   * @param dimensionsInfo service name + @ + application name
   * @param project
   * @return
   * @throws RemoteOperationException
   */
  public Map<String, String> loadFromConfigCenter(String dimensionsInfo, String project)
      throws RemoteOperationException {
    Response response = null;
    Map<String, String> result = new HashMap<>();
    try {
      response = httpTransport.sendGetRequest(
          configCenterConfig.getUrl() + "/" + ConfigConstants.DEFAULT_API_VERSION
              + "/" + project + "/configuration/items?dimensionsInfo="
              + URLEncoder.encode(dimensionsInfo, "UTF-8"));
      if (response == null) {
        return result;
      }
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.debug(response.getContent());
        Map<String, Map<String, String>> allConfigMap = JsonUtils.OBJ_MAPPER
            .readValue(response.getContent(),
                new TypeReference<Map<String, Map<String, String>>>() {
                });
        if (allConfigMap != null) {
          if (allConfigMap.get(ConfigConstants.APPLICATION_CONFIG) != null) {
            result.putAll(allConfigMap.get(ConfigConstants.APPLICATION_CONFIG));
          }
          if (dimensionsInfo.contains(ConfigConstants.DEFAULT_SERVICE_SEPARATOR)
              && allConfigMap.get(dimensionsInfo
              .substring(0, dimensionsInfo.indexOf(ConfigConstants.DEFAULT_SERVICE_SEPARATOR)))
              != null) {
            result.putAll(allConfigMap.get(dimensionsInfo
                .substring(0, dimensionsInfo.indexOf(ConfigConstants.DEFAULT_SERVICE_SEPARATOR))));
          }
          if (allConfigMap.get(dimensionsInfo) != null) {
            result.putAll(allConfigMap.get(dimensionsInfo));
          }
        }
        return result;
      } else if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        LOGGER.info(response.getStatusMessage());
        return null;
      } else {
        throw new RemoteOperationException(
            "read response failed. status=" + response.getStatusCode() + ";mesage=" + response
                .getStatusMessage());
      }
    } catch (RemoteServerUnavailableException e) {
      configCenterConfig.toggle();
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      configCenterConfig.toggle();
      throw new RemoteOperationException("read response failed. " + response, e);
    }
  }

  /**
   * @param serviceCombConfigProperties
   * @param project
   * @return
   * @throws RemoteOperationException
   */
  public Map<String, String> loadFromKie(ServiceCombConfigProperties serviceCombConfigProperties,
      String project)
      throws RemoteOperationException {
    Response response = null;
    Map<String, String> result = new HashMap<>();
    try {
      StringBuilder stringBuilder = new StringBuilder(configCenterConfig.getUrl());
      stringBuilder.append("/");
      stringBuilder.append(ConfigConstants.DEFAULT_KIE_API_VERSION);
      stringBuilder.append("/");
      stringBuilder.append(project);
      stringBuilder.append("/kie/kv?label=app:");
      stringBuilder.append(serviceCombConfigProperties.getAppName());
      response = httpTransport.sendGetRequest(stringBuilder.toString());
      if (response == null) {
        return result;
      }
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.debug(response.getContent());
        KVResponse allConfigList = JsonUtils.OBJ_MAPPER
            .readValue(response.getContent(), KVResponse.class);
        return KieUtil.getConfigByLabel(serviceCombConfigProperties, allConfigList);
      } else if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        LOGGER.info(response.getStatusMessage());
        return result;
      } else if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
        return result;
      } else {
        throw new RemoteOperationException(
            "read response failed. status=" + response.getStatusCode() + ";mesage=" + response
                .getStatusMessage());
      }
    } catch (Exception e) {
      configCenterConfig.toggle();
      throw new RemoteOperationException("read response failed. " + response, e);
    }
  }

}
