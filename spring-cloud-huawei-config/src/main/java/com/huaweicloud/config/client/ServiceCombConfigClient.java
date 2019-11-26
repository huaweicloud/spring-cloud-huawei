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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.exception.RemoteServerUnavailableException;
import com.huaweicloud.common.transport.HttpTransport;
import com.huaweicloud.common.transport.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @Author wangqijun
 * @Date 11:09 2019-10-17
 **/
public class ServiceCombConfigClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombConfigClient.class);

  private HttpTransport httpTransport;

  private String url;

  public ServiceCombConfigClient(String url, HttpTransport httpTransport) {
    this.httpTransport = httpTransport;
    this.url = url;
  }

  /**
   * load all remote config from config center
   * @param dimensionsInfo service name + @ + application name
   * @return
   * @throws RemoteOperationException
   */
  public Map<String, String> loadAll(String dimensionsInfo, String project) throws RemoteOperationException {
    Response response = null;
    Map<String, String> result = new HashMap<>();
    try {
      project = project != null && !project.isEmpty() ? project : ConfigConstants.DEFAULT_PROJECT;
      response = httpTransport.sendGetRequest(
          url + "/" + ConfigConstants.DEFAULT_API_VERSION + "/" + project + "/configuration/items?dimensionsInfo="
              + URLEncoder.encode(dimensionsInfo, "UTF-8"));
      if (response == null) {
        return result;
      }
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper objectMapper = new ObjectMapper();
        LOGGER.debug(response.getContent());
        Map<String, Map<String, String>> allConfigMap = objectMapper.readValue(response.getContent(), HashMap.class);
        if (allConfigMap != null) {
          if (allConfigMap.get(ConfigConstants.APPLICATION_CONFIG) != null) {
            result.putAll(allConfigMap.get(ConfigConstants.APPLICATION_CONFIG));
          }
          if (allConfigMap
              .get(dimensionsInfo.substring(0, dimensionsInfo.indexOf(ConfigConstants.DEFAULT_SERVICE_SEPARATOR)))
              != null) {
            result.putAll(allConfigMap
                .get(dimensionsInfo.substring(0, dimensionsInfo.indexOf(ConfigConstants.DEFAULT_SERVICE_SEPARATOR))));
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
            "read response failed. status=" + response.getStatusCode() + ";mesage=" + response.getStatusMessage());
      }
    } catch (RemoteServerUnavailableException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      throw new RemoteOperationException("read response failed. " + response, e);
    }
  }
}
