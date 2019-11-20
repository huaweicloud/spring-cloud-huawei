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

package org.springframework.cloud.servicecomb.discovery.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.common.exception.RemoteOperationException;
import org.springframework.cloud.common.exception.RemoteServerUnavailableException;
import org.springframework.cloud.common.exception.ServiceCombException;
import org.springframework.cloud.common.transport.HttpTransport;
import org.springframework.cloud.common.transport.Response;
import org.springframework.cloud.common.util.URLUtil;
import org.springframework.cloud.servicecomb.discovery.client.model.DependenciesArrayRequest;
import org.springframework.cloud.servicecomb.discovery.client.model.HeartbeatRequest;
import org.springframework.cloud.servicecomb.discovery.client.model.Microservice;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstance;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstanceSingleResponse;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstanceStatus;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstancesResponse;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceResponse;
import org.springframework.cloud.servicecomb.discovery.client.model.ServiceRegistryConfig;
import org.springframework.cloud.servicecomb.discovery.discovery.MicroserviceCache;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/
public class ServiceCombClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombClient.class);

  private List<String> serviceCenterUrlList = new ArrayList<>();

  private List<String> serviceCenterRegistryList = new ArrayList<>();

  private String urls;

  private long index = 0;

  private int registryUrlIndex = 0;

  private HttpTransport httpTransport;


  /**
   * Get a single instance
   * @param urls
   * @param autoDiscovery
   */
  public ServiceCombClient(String urls, HttpTransport httpTransport, boolean autoDiscovery) {
    this.httpTransport = httpTransport;
    this.urls = urls;
    dealMutiUrl(urls);
  }

  public void autoDiscovery(boolean autoDiscovery) {
    serviceCenterUrlList.add(serviceCenterRegistryList.get(registryUrlIndex));
    if (!autoDiscovery) {
      return;
    }
    try {
      MicroserviceInstancesResponse microserviceInstancesResponse = getServiceCenterInstances();
      for (MicroserviceInstance microserviceInstance : microserviceInstancesResponse.getInstances()) {
        if (microserviceInstance.getEndpoints() == null) {
          continue;
        }
        String endpoint = microserviceInstance.getEndpoints().get(0);
        if (MicroserviceInstanceStatus.UP == microserviceInstance.getStatus() && !URLUtil.isEquals(urls, endpoint)) {
          serviceCenterUrlList.add(URLUtil.transform(endpoint, "http"));
        }
      }
    } catch (RemoteOperationException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  private void dealMutiUrl(String urls) {
    if (urls != null && urls.indexOf(",") > 0) {
      for (String url : urls.split(",")) {
        if (url != null && !url.isEmpty()) {
          serviceCenterRegistryList.add(url);
        }
      }
    } else {
      serviceCenterRegistryList.add(urls);
    }
  }

  private String chooseServiceCenterUrl() {
    int count = serviceCenterUrlList.size();
    if (count > 0) {
      String result = serviceCenterUrlList.get((int) (index % count));
      index++;
      result = result + "/" + ServiceRegistryConfig.DEFAULT_API_VERSION + "/" + ServiceRegistryConfig.DEFAULT_PROJECT;
      LOGGER.info("choose service center, result=" + result);
      return result;
    }
    return null;
  }


  public MicroserviceInstancesResponse getServiceCenterInstances()
      throws RemoteOperationException {
    Response response = null;
    try {

      String formatUrl = buildDistributeURI("/registry/health");
      response = httpTransport.sendGetRequest(formatUrl);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper objectMapper = new ObjectMapper();
        MicroserviceInstancesResponse result = objectMapper
            .readValue(response.getContent(), MicroserviceInstancesResponse.class);
        LOGGER.info("getServiceCenterInstances result=" + result);
        return result;
      } else {
        throw new RemoteOperationException(
            "read response failed. status=" + response.getStatusCode() + ";mesage=" + response.getStatusMessage());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      handleRemoteOperationException(response, e);
    } catch (RemoteServerUnavailableException e) {
      handleRemoteOperationException(response, e);
    }
    return null;
  }

  /**
   * register to service-center
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
      response = httpTransport.sendPostRequest(buildDistributeURI("/registry/microservices"), stringEntity);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info(response.getContent());
        HashMap<String, String> result = objectMapper.readValue(response.getContent(), HashMap.class);
        if (null != result) {
          return result.get("serviceId");
        }
      } else {
        throw new RemoteOperationException(
            "read response failed. status=" + response.getStatusCode() + ";mesage=" + response.getStatusMessage());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      handleRemoteOperationException(response, e);
    }
    return null;
  }

  /**
   * query ServiceId
   * @param microservice
   * @return
   * @throws ServiceCombException
   */
  public String getServiceId(Microservice microservice) throws ServiceCombException {
    Response response = null;
    try {
      String path =
          serviceCenterRegistryList.get(registryUrlIndex) + "/" + ServiceRegistryConfig.DEFAULT_API_VERSION + "/"
              + ServiceRegistryConfig.DEFAULT_PROJECT + "/registry/existence";
      response = httpTransport.sendGetRequest(buildURI(path, "microservice", microservice));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper objectMapper = new ObjectMapper();
        LOGGER.info(response.getContent());
        HashMap<String, String> result = objectMapper.readValue(response.getContent(), HashMap.class);
        return result.get("serviceId");
      } else if (response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        LOGGER.info(response.getStatusMessage());
        return null;
      } else {
        throw new RemoteOperationException(
            "read response failed. status=" + response.getStatusCode() + ";mesage=" + response.getStatusMessage());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      handleRemoteOperationException(response, e);
    }
    return null;
  }

  /**
   *
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
      String formatUrl = buildDistributeURI(
          "/registry/microservices/" + microserviceInstance.getServiceId() + "/instances");
      response = httpTransport
          .sendPostRequest(formatUrl,
              stringEntity);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info(response.getContent());
        HashMap<String, String> result = objectMapper.readValue(response.getContent(), HashMap.class);
        return result.get("instanceId");
      } else {
        throw new RemoteOperationException(
            "read response failed. url=" + formatUrl + "status=" + response.getStatusCode() + ";mesage=" + response
                .getStatusMessage());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      handleRemoteOperationException(response, e);
    }
    return null;
  }

  /**
   *  deRegister Instance
   * @param serviceId
   * @param instanceId
   * @throws ServiceCombException
   */
  public boolean deRegisterInstance(String serviceId, String instanceId)
      throws ServiceCombException {
    Response response = null;
    try {
      String formatUrl = buildURI("/registry/microservices/" + serviceId + "/instances/" + instanceId);
      response = httpTransport
          .sendDeleteRequest(formatUrl);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("deRegister success.");
        return true;
      } else {
        throw new RemoteOperationException(
            "deRegister failed. url=" + formatUrl + "status=" + response.getStatusCode() + ";mesage=" + response
                .getStatusMessage());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    }
  }

  /**
   * query instances of one service
   * @param microservice
   * @return
   * @throws ServiceCombException
   */
  public List<ServiceInstance> getInstances(Microservice microservice)
      throws ServiceCombException {
    List<ServiceInstance> instanceList = new ArrayList<>();
    Response response = null;
    try {
      response = httpTransport
          .sendGetRequest(buildURI(chooseServiceCenterUrl() + "/registry/instances", null, microservice));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        LOGGER.info(response.getContent());
        Microservice result = objectMapper.readValue(response.getContent(), Microservice.class);
        if (result == null || result.getInstances() == null) {
          return instanceList;
        }
        MicroserviceCache.initInsList(result.getInstances(),microservice.getServiceName());
        for (MicroserviceInstance instance : result.getInstances()) {
          if (instance.getStatus() != MicroserviceInstanceStatus.UP) {
            continue;
          }
          int port;
          String host;
          if (!instance.getEndpoints().isEmpty()) {
            String endpoint = instance.getEndpoints().get(0);
            URI endpointURIBuilder = new URIBuilder(endpoint).build();
            port = endpointURIBuilder.getPort();
            host = endpointURIBuilder.getHost();
          } else {
            throw new RemoteOperationException(
                "read response failed. status=" + response.getStatusCode() + ";mesage=" + response.getStatusMessage());
          }
          instanceList.add(
              new DefaultServiceInstance(instance.getInstanceId(), instance.getServiceId(), host, port, false));
        }
      } else {
        throw new RemoteOperationException(
            "read response failed. status=" + response.getStatusCode() + ";mesage=" + response.getStatusMessage());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      handleRemoteOperationException(response, e);
    }
    return instanceList;
  }

  public void addDependencies(DependenciesArrayRequest dependenciesArrayRequest)
      throws ServiceCombException {
    MicroserviceInstanceSingleResponse result = null;
    Response response = null;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String content = objectMapper.writeValueAsString(dependenciesArrayRequest);
      StringEntity stringEntity = new StringEntity(content, "utf-8");
      response = httpTransport
          .sendPutRequest(buildURI("/registry/dependencies"), stringEntity);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info("update dependencies success.");
      } else {
        throw new RemoteOperationException(
            "read response failed. status=" + response.getStatusCode() + ";mesage=" + response.getStatusMessage());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      handleRemoteOperationException(response, e);
    }
  }

  public MicroserviceInstanceSingleResponse getInstance(String serviceId, String instanceId)
      throws ServiceCombException {
    MicroserviceInstanceSingleResponse result = null;
    Response response = null;
    try {
      response = httpTransport
          .sendGetRequest(buildURI("/registry/microservices/" + serviceId + "/instances/" + instanceId));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        LOGGER.info(response.getContent());
        result = objectMapper.readValue(response.getContent(), MicroserviceInstanceSingleResponse.class);
      } else {
        throw new RemoteOperationException(
            "read response failed. status=" + response.getStatusCode() + ";mesage=" + response.getStatusMessage());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      handleRemoteOperationException(response, e);
    }
    return result;
  }

  /**
   *
   * @param heartbeatRequest
   * @throws ServiceCombException
   */
  public void heartbeat(HeartbeatRequest heartbeatRequest) throws ServiceCombException {
    Response response = null;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String content = objectMapper.writeValueAsString(heartbeatRequest);
      StringEntity stringEntity = new StringEntity(content, "utf-8");
      response = httpTransport.sendPutRequest(buildDistributeURI("/registry/heartbeats"), stringEntity);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info(" heartbeat success.");
      } else {
        throw new RemoteOperationException(
            "read response failed. status=" + response.getStatusCode() + ";mesage=" + response.getStatusMessage());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      handleRemoteOperationException(response, e);
    }
  }

  public boolean updateInstanceStatus(String serviceId, String instanceId, String status) throws ServiceCombException {
    Response response = null;
    try {
      StringEntity stringEntity = new StringEntity("", "utf-8");
      response = httpTransport.sendPutRequest(
          buildURI("/registry/microservices/" + serviceId + "/instances/" + instanceId + "/status?value=" + status),
          stringEntity);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info(" update instance status success.");
        return true;
      } else {
        throw new RemoteOperationException(
            "update instance status failed. status=" + response.getStatusCode() + ";mesage=" + response
                .getStatusMessage());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    }
  }

  public MicroserviceResponse getServices() throws ServiceCombException {
    MicroserviceResponse result = null;
    Response response = null;
    try {
      if (serviceCenterUrlList == null || serviceCenterUrlList.isEmpty()) {
        LOGGER.warn("wait autodiscovery.");
        return result;
      }
      response = httpTransport.sendGetRequest(
          buildURI("/registry/microservices"));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        result = objectMapper.readValue(response.getContent(), MicroserviceResponse.class);
      } else {
        throw new RemoteOperationException(
            "read response failed. status=" + response.getStatusCode() + ";mesage=" + response.getStatusMessage());
      }
    } catch (URISyntaxException e) {
      throw new RemoteOperationException("build url failed.", e);
    } catch (IOException e) {
      handleRemoteOperationException(response, e);
    }
    return result;
  }

  private String buildURI(String path) throws URISyntaxException {
    URIBuilder uriBuilder = new URIBuilder(chooseServiceCenterUrl() + path);
    return uriBuilder.build().toString();
  }

  private String buildDistributeURI(String path) throws URISyntaxException {
    path = serviceCenterRegistryList.get(registryUrlIndex) + "/" + ServiceRegistryConfig.DEFAULT_API_VERSION + "/"
        + ServiceRegistryConfig.DEFAULT_PROJECT + path;
    URIBuilder uriBuilder = new URIBuilder(path);
    return uriBuilder.build().toString();
  }

  private String buildURI(String path, String type, Microservice microservice) throws URISyntaxException {
    URIBuilder uriBuilder = new URIBuilder(path);
    if (null != type) {
      uriBuilder.setParameter("type", "microservice");
    }
    uriBuilder.setParameter("appId", microservice.getAppId());
    uriBuilder.setParameter("serviceName", microservice.getServiceName());
    uriBuilder.setParameter("version", microservice.getVersion());
    return uriBuilder.build().toString();
  }

  /**
   * Merge message
   * @param response
   * @param e
   * @throws RemoteOperationException
   */
  private void handleRemoteOperationException(Response response, Exception e) throws RemoteOperationException {
    String message = "read response failed. ";
    if (null != response) {
      message = message + response;
    }
    throw new RemoteOperationException(message, e);
  }

  public synchronized void toggle() {
    registryUrlIndex = (registryUrlIndex + 1) % serviceCenterRegistryList.size();
  }
}