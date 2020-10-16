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

package com.huaweicloud.servicecomb.discovery.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.huaweicloud.common.cache.RegisterCache;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.exception.RemoteServerUnavailableException;
import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.common.transport.HttpTransport;
import com.huaweicloud.common.transport.Response;
import com.huaweicloud.common.transport.URLConfig;
import com.huaweicloud.common.util.URLUtil;
import com.huaweicloud.servicecomb.discovery.client.model.HeardBeatStatus;
import com.huaweicloud.servicecomb.discovery.client.model.HeartbeatRequest;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstance;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstanceSingleResponse;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstancesResponse;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceResponse;
import com.huaweicloud.servicecomb.discovery.client.model.SchemaRequest;
import com.huaweicloud.servicecomb.discovery.client.model.SchemaResponse;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceRegistryConfig;
import com.huaweicloud.servicecomb.discovery.discovery.MicroserviceCache;
import com.huaweicloud.servicecomb.discovery.discovery.MicroserviceHandler;


/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/
public class ServiceCombClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombClient.class);

  private URLConfig registryConfig = new URLConfig();

  public static String INSTANCE_STATUS = "status";

  public static String ZONE = "zone";

  private HttpTransport httpTransport;

  /**
   * Get a single instance
   *
   * @param urls
   */
  public ServiceCombClient(String urls, HttpTransport httpTransport) {
    this.httpTransport = httpTransport;
    registryConfig.addUrl(URLUtil.getEnvServerURL());
    if (registryConfig.isEmpty()) {
      registryConfig.addUrl(URLUtil.dealMultiUrl(urls));
    }
  }

  public void autoDiscovery(boolean autoDiscovery) {
    if (!autoDiscovery) {
      return;
    }
    try {
      MicroserviceInstancesResponse microserviceInstancesResponse = getServiceCenterInstances();
      for (MicroserviceInstance microserviceInstance : microserviceInstancesResponse
          .getInstances()) {
        if (microserviceInstance.getEndpoints() == null) {
          continue;
        }
        registryConfig.addUrlAfterDnsResolve(
            microserviceInstance.getEndpoints().stream()
                .filter(url -> !url.contains("[::]") && url.startsWith("rest"))
                .map(URLUtil::transform)
                .collect(Collectors.toList()));
      }
    } catch (RemoteOperationException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }


  public MicroserviceInstancesResponse getServiceCenterInstances()
      throws RemoteOperationException {
    Response response = new Response();
    try {
      String formatUrl = buildURI("/registry/health");
      response = httpTransport.sendGetRequest(formatUrl);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        MicroserviceInstancesResponse result = JsonUtils.OBJ_MAPPER
            .readValue(response.getContent(), MicroserviceInstancesResponse.class);
        LOGGER.info("getServiceCenterInstances result=" + result);
        return result;
      }
      throw new RemoteOperationException(
          "read response failed. status:" + response.getStatusCode() + "; message:" + response
              .getStatusMessage() + "; content:" + response.getContent());
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException | RemoteServerUnavailableException e) {
      throw new RemoteOperationException("read response failed. " + response, e);
    }
  }

  /**
   * register to service-center
   *
   * @param microservice
   * @return
   * @throws ServiceCombException
   */
  public String registerMicroservice(Microservice microservice) throws ServiceCombException {
    Response response = null;
    String content = null;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
      content = objectMapper.writeValueAsString(microservice);
      StringEntity stringEntity = new StringEntity(content, "utf-8");
      response = httpTransport
          .sendPostRequest(buildURI("/registry/microservices"), stringEntity);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info(response.getContent());
        HashMap<String, String> result = objectMapper
            .readValue(response.getContent(), HashMap.class);
        if (null != result) {
          return result.get("serviceId");
        }
      } else {
        throw new RemoteOperationException(
            "read response failed. status:" + response.getStatusCode() + "; message:" + response
                .getStatusMessage() + "; content:" + response.getContent());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      throw new RemoteOperationException(
          response == null ? "read response failed. " : "read response failed. " + response, e);
    }
    return null;
  }

  /**
   * query ServiceId
   *
   * @param microservice
   * @return
   * @throws ServiceCombException
   */
  public String getServiceId(Microservice microservice) throws ServiceCombException {
    Response response = null;
    try {
      String path = buildURI("/registry/existence");
      response = httpTransport
          .sendGetRequest(addParam2URI(path, "microservice", null, microservice));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info(response.getContent());
        HashMap<String, String> result = JsonUtils.OBJ_MAPPER
            .readValue(response.getContent(), HashMap.class);
        return result.get("serviceId");
      } else if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        LOGGER.info(response.getContent());
        return null;
      } else {
        throw new RemoteOperationException(
            "read response failed. status:" + response.getStatusCode() + "; message:" + response
                .getStatusMessage() + "; content:" + response.getContent());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      throw new RemoteOperationException("read response failed. " + response, e);
    }
  }

  public SchemaResponse getSchemas(String serviceId) throws ServiceCombException {
    Response response = null;
    try {
      response = httpTransport
          .sendGetRequest(buildURI("/registry/microservices/" + serviceId + "/schemas"));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        return JsonUtils.OBJ_MAPPER
            .readValue(response.getContent(), SchemaResponse.class);
      } else if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        LOGGER.info(response.getContent());
        return null;
      } else {
        throw new RemoteOperationException(
            "read response failed. status:" + response.getStatusCode() + "; message:" + response
                .getStatusMessage() + "; content:" + response.getContent());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      throw new RemoteOperationException("read response failed. " + response, e);
    }
  }

  /**
   * @param microserviceInstance
   * @return
   * @throws ServiceCombException
   */
  public String registerInstance(MicroserviceInstance microserviceInstance)
      throws ServiceCombException {
    Response response = null;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
      String content = objectMapper.writeValueAsString(microserviceInstance);
      StringEntity stringEntity = new StringEntity(content, "utf-8");
      String formatUrl = buildURI(
          "/registry/microservices/" + microserviceInstance.getServiceId() + "/instances");
      response = httpTransport
          .sendPostRequest(formatUrl,
              stringEntity);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info(response.getContent());
        HashMap<String, String> result = objectMapper
            .readValue(response.getContent(), HashMap.class);
        return result.get("instanceId");
      } else {
        throw new RemoteOperationException(
            "read response failed. url:" + formatUrl + "status:" + response.getStatusCode()
                + "; message:" + response.getStatusMessage() + "; content:" + response
                .getContent());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      throw new RemoteOperationException(
          response == null ? "read response failed. " : "read response failed. " + response, e);
    }
  }

  /**
   * deRegister Instance
   *
   * @param serviceId
   * @param instanceId
   * @throws ServiceCombException
   */
  public boolean deRegisterInstance(String serviceId, String instanceId)
      throws ServiceCombException {
    Response response = null;
    try {
      String formatUrl = buildURI(
          "/registry/microservices/" + serviceId + "/instances/" + instanceId);
      response = httpTransport
          .sendDeleteRequest(formatUrl);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("deRegister success.");
        return true;
      } else {
        throw new RemoteOperationException(
            "deRegister failed. url:" + formatUrl + "status:" + response.getStatusCode()
                + "; message:" + response.getStatusMessage() + "; content:" + response
                .getContent());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    }
  }

  /**
   * query instances of one service
   * returns null when an error occurs or http response is not 200(data not modified)
   * when return null, we do not want the data to change or be cleaned
   *
   *
   * @param microservice
   * @return
   * @throws ServiceCombException
   */
  public List<ServiceInstance> getInstances(Microservice microservice, String revision) {
    List<ServiceInstance> instanceList = new ArrayList<>();
    Response response = null;
    try {
      Map<String, String> heades = Maps.newHashMap();
      String CONSUMER_HEADER = "X-ConsumerId";
      heades.put(CONSUMER_HEADER, RegisterCache.getServiceID());
      // rev是一个query字段，单位是app service version,需要缓存
      response = httpTransport
          .sendGetRequest(
              addParam2URI(buildURI("/registry/instances"), null, revision,
                  microservice),
              heades);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        Microservice result = JsonUtils.OBJ_MAPPER
            .readValue(response.getContent(), Microservice.class);
        if (result == null || result.getInstances() == null) {
          return instanceList;
        }
        String REVISION_HEADER = "X-Resource-Revision";
        if (!StringUtils.isEmpty(response.getHeader(REVISION_HEADER))) {
          MicroserviceHandler.serviceRevision.put(
              microservice.getServiceName(), response.getHeader(REVISION_HEADER));
        }
        MicroserviceCache.initInsList(result.getInstances(), microservice.getServiceName());
        for (MicroserviceInstance instance : result.getInstances()) {
          for (String endpoint : instance.getEndpoints()) {
            if (!endpoint.startsWith("rest://")) {
              continue;
            }
            URI endpointURIBuilder = new URIBuilder(endpoint).build();
            int port = endpointURIBuilder.getPort();
            String host = endpointURIBuilder.getHost();

            Map<String, String> map = new HashMap<>();
            map.put(INSTANCE_STATUS, instance.getStatus().name());
            if (instance.getDataCenterInfo() != null) {
              map.put(ZONE, instance.getDataCenterInfo().getZone());
            }
            instanceList.add(
                new DefaultServiceInstance(instance.getInstanceId(), instance.getServiceName(), host,
                    port, false, map));
          }
        }
        return instanceList;
      } else if (response.getStatusCode() != HttpStatus.SC_NOT_MODIFIED) {
        LOGGER.debug(
            "read response failed. status:" + response.getStatusCode() + "; message:" + response
                .getStatusMessage() + "; content:" + response.getContent());
      }
    } catch (URISyntaxException e) {
      LOGGER.warn("build url failed.", e);
    } catch (IOException e) {
      LOGGER.warn("read response failed. " + response);
    } catch (RemoteServerUnavailableException e) {
      LOGGER.warn("get instances failed.", e);
    }
    return null;
  }

  public MicroserviceInstanceSingleResponse getInstance(String serviceId, String instanceId)
      throws ServiceCombException {
    MicroserviceInstanceSingleResponse result = null;
    Response response = null;
    try {
      response = httpTransport
          .sendGetRequest(
              buildURI("/registry/microservices/" + serviceId + "/instances/" + instanceId));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info(response.getContent());
        result = JsonUtils.OBJ_MAPPER
            .readValue(response.getContent(), MicroserviceInstanceSingleResponse.class);
      } else {
        throw new RemoteOperationException(
            "read response failed. status:" + response.getStatusCode() + "; message:" + response
                .getStatusMessage() + "; content:" + response.getContent());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      throw new RemoteOperationException("read response failed. " + response, e);
    }
    return result;
  }

  /**
   * @param heartbeatRequest
   * @throws ServiceCombException
   */
  public HeardBeatStatus heartbeat(HeartbeatRequest heartbeatRequest) throws ServiceCombException {
    Response response = null;
    try {
      String content = JsonUtils.OBJ_MAPPER.writeValueAsString(heartbeatRequest);
      StringEntity stringEntity = new StringEntity(content, "utf-8");
      response = httpTransport
          .sendPutRequest(buildURI("/registry/heartbeats"), stringEntity);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.debug("heartbeat success.");
        return HeardBeatStatus.SUCCESS;
      } else if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        LOGGER.error(
            "heartbeat to service center failed. status:" + response.getStatusCode() + "; message:"
                + response.getStatusMessage() + "; content:" + response.getContent());
        return HeardBeatStatus.FAILED;
      }
      throw new IOException();
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      toggle();
      throw new RemoteOperationException("read response failed , msg: {} " + response, e);
    }
  }

  public boolean updateInstanceStatus(String serviceId, String instanceId, String status)
      throws ServiceCombException {
    Response response = null;
    try {
      StringEntity stringEntity = new StringEntity("", "utf-8");
      response = httpTransport.sendPutRequest(
          buildURI(
              "/registry/microservices/" + serviceId + "/instances/" + instanceId + "/status?value="
                  + status),
          stringEntity);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info(" update instance status success.");
        return true;
      }
      throw new RemoteOperationException(
          "update instance status failed. status:" + response.getStatusCode() + "; message:"
              + response.getStatusMessage() + "; content:" + response.getContent());
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    }
  }

  public MicroserviceResponse getServices() throws ServiceCombException {
    MicroserviceResponse result = null;
    Response response = null;
    try {
      //previous code will return null
      response = httpTransport.sendGetRequest(
          buildURI("/registry/microservices"));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        result = JsonUtils.OBJ_MAPPER.readValue(response.getContent(), MicroserviceResponse.class);
      } else {
        throw new RemoteOperationException(
            "read response failed. status:" + response.getStatusCode() + "; message:" + response
                .getStatusMessage() + "; content:" + response.getContent());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      throw new RemoteOperationException("read response failed. " + response, e);
    }
    return result;
  }

  public boolean registerSchema(String microserviceId, String schemaId, String schemaContent)
      throws RemoteOperationException {
    Response response = null;
    SchemaRequest request = new SchemaRequest();
    request.setSchema(schemaContent);
    request.setSummary(calcSchemaSummary(schemaContent));
    try {
      String formatUrl = buildURI(
          "/registry/microservices/" + microserviceId + "/schemas/" + schemaId);
      byte[] body = JsonUtils.OBJ_MAPPER.writeValueAsBytes(request);
      ByteArrayEntity byteArrayEntity = new ByteArrayEntity(body);
      response = httpTransport.sendPutRequest(formatUrl, byteArrayEntity);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("register schema {}/{} success.", microserviceId, schemaId);
        return true;
      }
      LOGGER.error("Register schema {}/{} failed, code:{} , msg:{} ", microserviceId, schemaId,
          response.getStatusCode(), response.getContent());
      return false;
    } catch (JsonProcessingException e) {
      LOGGER.error("registerSchema serialization failed : {}", e.getMessage());
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (RemoteServerUnavailableException e) {
      throw new RemoteOperationException("read response failed. ", e);
    }
    return false;
  }

  public static String calcSchemaSummary(String schemaContent) {
    return Hashing.sha256().newHasher().putString(schemaContent, Charsets.UTF_8).hash().toString();
  }

  private String buildURI(String path) throws URISyntaxException {
    path = registryConfig.getUrl() + "/"
        + ServiceRegistryConfig.DEFAULT_API_VERSION + "/"
        + ServiceRegistryConfig.DEFAULT_PROJECT + path;
    URIBuilder uriBuilder = new URIBuilder(path);
    return uriBuilder.build().toString();
  }

  //just add param
  private String addParam2URI(String path, String type, String revision, Microservice microservice)
      throws URISyntaxException {
    URIBuilder uriBuilder = new URIBuilder(path);
    if (null != type) {
      uriBuilder.setParameter("type", type);
    }
    if (null != revision) {
      uriBuilder.setParameter("rev", revision);
    }
    uriBuilder.setParameter("appId", microservice.getAppId());
    uriBuilder.setParameter("serviceName", microservice.getServiceName());
    uriBuilder.setParameter("version", microservice.getVersion());
    uriBuilder.setParameter("env", microservice.getEnvironment());
    return uriBuilder.build().toString();
  }

  public void toggle() {
    registryConfig.toggle();
  }

  public String getUrl() {
    return registryConfig.getUrl();
  }
}
