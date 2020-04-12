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
import com.huaweicloud.common.discovery.KieAddrSeeker;
import com.huaweicloud.common.transport.URLConfig;
import com.huaweicloud.common.util.URLUtil;
import com.huaweicloud.config.ServiceCombConfigProperties;
import com.huaweicloud.config.kie.KVBody;
import com.huaweicloud.config.kie.KVResponse;
import com.huaweicloud.config.kie.KieUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.exception.RemoteServerUnavailableException;
import com.huaweicloud.common.transport.HttpTransport;
import com.huaweicloud.common.transport.Response;
import org.springframework.util.StringUtils;

/**
 * @Author wangqijun
 * @Date 11:09 2019-10-17
 **/
public class ServiceCombConfigClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombConfigClient.class);

  private HttpTransport httpTransport;

  private URLConfig configCenterConfig = new URLConfig();

  //todo: set false to active
  private AtomicBoolean isupdatedConfig = new AtomicBoolean(true);

  private AtomicBoolean isFirst = new AtomicBoolean(true);

  private String revision = "0";

  public ServiceCombConfigClient(String urls, HttpTransport httpTransport) {
    this.httpTransport = httpTransport;
    configCenterConfig.addUrl(URLUtil.getEnvConfigUrl());
    if (configCenterConfig.isEmpty()) {
      configCenterConfig.addUrl(URLUtil.dealMultiUrl(urls));
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
      //上传本地配置
      if (!isupdatedConfig.get() && updateToKie(serviceCombConfigProperties)) {
        isupdatedConfig.compareAndSet(false, true);
      }
      if (serviceCombConfigProperties.getEnableLongPolling()) {
        return loadFromKie(serviceCombConfigProperties, project, true);
      } else {
        return loadFromKie(serviceCombConfigProperties, project, false);
      }
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
              + URLEncoder.encode(dimensionsInfo, "UTF-8") + "&revision=" + revision);
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
          if (allConfigMap.get(ConfigConstants.REVISION) != null) {
            revision = allConfigMap.get(ConfigConstants.REVISION).get("version");
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
      String project, boolean isWatch)
      throws RemoteOperationException {
    Response response = null;
    Map<String, String> result = new HashMap<>();
    try {
      String stringBuilder = configCenterConfig.getUrl()
          + "/"
          + ConfigConstants.DEFAULT_KIE_API_VERSION
          + "/"
          + project
          + "/kie/kv?label=app:"
          + serviceCombConfigProperties.getAppName()
          + "&revision="
          + revision;
      if (isWatch && !isFirst.get()) {
        stringBuilder +=
            "&wait=" + serviceCombConfigProperties.getWatch().getPollingWaitTimeInSeconds() + "s";
      }
      isFirst.compareAndSet(true, false);
      response = httpTransport.sendGetRequest(stringBuilder);
      if (response == null) {
        return null;
      }
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        revision = response.getHeader("X-Kie-Revision");
        LOGGER.debug(response.getContent());
        KVResponse allConfigList = JsonUtils.OBJ_MAPPER
            .readValue(response.getContent(), KVResponse.class);
        return KieUtil.getConfigByLabel(serviceCombConfigProperties, allConfigList);
      } else if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        LOGGER.info(response.getStatusMessage());
        return null;
      } else if (response.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
        return null;
      } else {
        throw new RemoteOperationException(
            "read response failed. status:" + response.getStatusCode() + "; message:" + response
                .getStatusMessage() + "; content:" + response.getContent());
      }
    } catch (Exception e) {
      configCenterConfig.toggle();
      throw new RemoteOperationException("read response failed. " + response, e);
    }
  }

  /**
   * todo : update the all file
   *
   * @param serviceCombConfigProperties
   * @return
   */
  public boolean updateToKie(ServiceCombConfigProperties serviceCombConfigProperties) {
    String key = "application.yaml";
    KVBody kvBody = new KVBody();
    kvBody.initLabels(serviceCombConfigProperties);
    kvBody.setValue("");
    Response response = null;
    try {
      String content = JsonUtils.OBJ_MAPPER.writeValueAsString(kvBody);
      StringEntity stringEntity = new StringEntity(content, "utf-8");
      response = httpTransport.sendPutRequest("/kie/kv/" + key, stringEntity);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return true;
      } else {
        LOGGER.error(
            "create keyValue fails, responseStatusCode:{}, responseMessage:{}, responseContent:{}",
            response.getStatusCode(), response.getStatusMessage(), response.getContent());
        return false;
      }
    } catch (IOException e) {
      LOGGER.error("create keyValue fails", e);
    } catch (RemoteServerUnavailableException e) {
      LOGGER.error("putKeyValue to kie server failed, response= {}", response);
    }
    return false;
  }
}
