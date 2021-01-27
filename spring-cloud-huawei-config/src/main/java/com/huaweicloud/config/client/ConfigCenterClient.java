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
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.exception.RemoteServerUnavailableException;
import com.huaweicloud.common.transport.HttpTransport;
import com.huaweicloud.common.transport.Response;
import com.huaweicloud.config.ServiceCombConfigProperties;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @Author GuoYl123
 * @Date 2020/7/14
 **/
public class ConfigCenterClient extends ServiceCombConfigClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCenterClient.class);

  public ConfigCenterClient(String urls,
      HttpTransport httpTransport) {
    super(urls, httpTransport);
  }

  public Map<String, Object> loadAll(ServiceCombConfigProperties serviceCombConfigProperties,
      String project) throws RemoteOperationException {
    project = StringUtils.isEmpty(project) ? ConfigConstants.DEFAULT_PROJECT : project;
    String dimensionsInfo = spliceDimensionsInfo(serviceCombConfigProperties);
    Map<String, String> headers = new HashMap<>();
    headers.put("x-environment", serviceCombConfigProperties.getEnv());
    Response response = null;
    Map<String, Object> result = new HashMap<>();
    try {
      response = httpTransport.sendGetRequest(
          configCenterConfig.getUrl() + "/" + ConfigConstants.DEFAULT_API_VERSION
              + "/" + project + "/configuration/items?dimensionsInfo="
              + URLEncoder.encode(dimensionsInfo, "UTF-8") + "&revision=" + revision, headers);
      if (response == null) {
        return result;
      }
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.debug(response.getContent());
        Map<String, Map<String, Object>> allConfigMap = JsonUtils.OBJ_MAPPER
            .readValue(response.getContent(),
                new TypeReference<Map<String, Map<String, Object>>>() {
                });
        if (allConfigMap != null) {
          if (allConfigMap.get(ConfigConstants.REVISION) != null) {
            revision = (String) allConfigMap.get(ConfigConstants.REVISION).get("version");
          }
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
      } else if (response.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
        return null;
      } else if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        LOGGER.info(response.getStatusMessage());
        return null;
      } else {
        throw new RemoteOperationException(
            "read response failed. status:"
                + response.getStatusCode()
                + "; message:"
                + response.getStatusMessage()
                + "; content:"
                + response.getContent());
      }
    } catch (RemoteServerUnavailableException e) {
      configCenterConfig.toggle();
      throw new RemoteOperationException(
          "config center address is not available , will retry.", e);
    } catch (IOException e) {
      configCenterConfig.toggle();
      throw new RemoteOperationException("read response failed. " + response, e);
    }
  }


  private String spliceDimensionsInfo(ServiceCombConfigProperties serviceCombConfigProperties) {
    String result =
        serviceCombConfigProperties.getServiceName() + ConfigConstants.DEFAULT_APP_SEPARATOR
            + serviceCombConfigProperties.getAppName();
    if (!StringUtils.isEmpty(serviceCombConfigProperties.getVersion())) {
      result = result + ConfigConstants.DEFAULT_SERVICE_SEPARATOR + serviceCombConfigProperties
          .getVersion();
    }
    return result;
  }
}
