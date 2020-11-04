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

import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.exception.ServiceCombRuntimeException;
import com.huaweicloud.common.log.ServiceCombLogProperties;
import com.huaweicloud.common.log.logConstantValue;
import com.huaweicloud.common.transport.HttpTransport;
import com.huaweicloud.common.transport.Response;
import com.huaweicloud.config.ServiceCombConfigProperties;
import com.huaweicloud.config.model.KVDoc;
import com.huaweicloud.config.model.KVResponse;
import com.huaweicloud.config.model.ValueType;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.http.HttpStatus;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StringUtils;

/**
 * @Author GuoYl123
 * @Date 2020/7/14
 **/
public class KieClient extends ServiceCombConfigClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(KieClient.class);

  @Autowired
  private ServiceCombLogProperties serviceCombLogProperties;

  private AtomicBoolean isFirst = new AtomicBoolean(true);

  public KieClient(String urls,
      HttpTransport httpTransport) {
    super(urls, httpTransport);
  }

  public Map<String, Object> loadAll(ServiceCombConfigProperties serviceCombConfigProperties,
      String project) throws RemoteOperationException {
    project = project != null && !project.isEmpty() ? project : ConfigConstants.DEFAULT_PROJECT;
    boolean isWatch = false;
    if (serviceCombConfigProperties.getEnableLongPolling()) {
      isWatch = true;
    }
    Response response = null;
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
        return getConfigByLabel(serviceCombConfigProperties, allConfigList);
      } else if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        LOGGER.info(response.getStatusMessage());
        LOGGER.info(serviceCombLogProperties.generateStructureLog(
            "bad request from serviceCenter.",
            logConstantValue.LOG_LEVEL_INFO, logConstantValue.MODULE_CONFIG,
            logConstantValue.EVENT_REQUEST));
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


  private Map<String, Object> getConfigByLabel(
      ServiceCombConfigProperties serviceCombConfigProperties, KVResponse resp) {
    Map<String, Object> resultMap = new HashMap<>();
    List<KVDoc> appList = new ArrayList<>();
    List<KVDoc> serviceList = new ArrayList<>();
    List<KVDoc> versionList = new ArrayList<>();
    for (KVDoc kvDoc : resp.getData()) {
      if (!StringUtils.isEmpty(kvDoc.getStatus()) && !kvDoc.getStatus()
          .equals(ConfigConstants.STATUS_ENABLED)) {
        continue;
      }
      Map<String, String> labelsMap = kvDoc.getLabels();
      if (labelsMap.containsKey(ConfigConstants.LABEL_APP) && labelsMap
          .get(ConfigConstants.LABEL_APP)
          .equals(serviceCombConfigProperties.getAppName())
          && labelsMap.containsKey(ConfigConstants.LABEL_ENV) && labelsMap
          .get(ConfigConstants.LABEL_ENV)
          .equals(serviceCombConfigProperties.getEnv())) {
        if (!labelsMap.containsKey(ConfigConstants.LABEL_SERVICE)) {
          appList.add(kvDoc);
        }
        if (labelsMap.containsKey(ConfigConstants.LABEL_SERVICE) && labelsMap
            .get(ConfigConstants.LABEL_SERVICE)
            .equals(serviceCombConfigProperties.getServiceName())) {
          if (!kvDoc.getLabels().containsKey(ConfigConstants.LABEL_VERSION)) {
            serviceList.add(kvDoc);
          }
          if (labelsMap.containsKey(ConfigConstants.LABEL_VERSION) && labelsMap
              .get(ConfigConstants.LABEL_VERSION)
              .equals(serviceCombConfigProperties.getVersion())) {
            versionList.add(kvDoc);
          }
        }
      }
    }
    //kv is priority
    for (KVDoc kvDoc : appList) {
      resultMap.putAll(processValueType(kvDoc));
    }
    for (KVDoc kvDoc : serviceList) {
      resultMap.putAll(processValueType(kvDoc));
    }
    for (KVDoc kvDoc : versionList) {
      resultMap.putAll(processValueType(kvDoc));
    }
    return resultMap;
  }


  private Map<String, Object> processValueType(KVDoc kvDoc) {
    ValueType vtype;
    try {
      vtype = ValueType.valueOf(kvDoc.getValueType());
    } catch (IllegalArgumentException e) {
      throw new ServiceCombRuntimeException("value type not support");
    }
    Properties properties = new Properties();
    Map<String, Object> kvMap = new HashMap<>();
    try {
      switch (vtype) {
        case yml:
        case yaml:
          YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
          yamlFactory.setResources(new ByteArrayResource(kvDoc.getValue().getBytes()));
          return toMap(kvDoc.getKey(), yamlFactory.getObject());
        case properties:
          properties.load(new StringReader(kvDoc.getValue()));
          return toMap(kvDoc.getKey(), properties);
        case text:
        case string:
        default:
          kvMap.put(kvDoc.getKey(), kvDoc.getValue());
          return kvMap;
      }
    } catch (Exception e) {
      LOGGER.error("read config failed");
    }
    return Collections.emptyMap();
  }


  private Map<String, Object> toMap(String prefix, Properties properties) {
    if (properties == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> result = new HashMap<>();
    Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      Object value = properties.getProperty(key);
      if (!StringUtils.isEmpty(prefix)) {
        key = prefix + "." + key;
      }
      if (value != null) {
        result.put(key, value);
      } else {
        result.put(key, null);
      }
    }
    return result;
  }
}
