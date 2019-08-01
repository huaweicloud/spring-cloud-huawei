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
import org.springframework.cloud.servicecomb.discovery.client.exception.RemoteOperationException;
import org.springframework.cloud.servicecomb.discovery.client.exception.ServiceCombException;
import org.springframework.cloud.servicecomb.discovery.client.model.HeartbeatRequest;
import org.springframework.cloud.servicecomb.discovery.client.model.Microservice;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstance;
import org.springframework.cloud.servicecomb.discovery.client.model.Response;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/
public class ServiceCombClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombClient.class);

  private String url;

  private HttpTransport httpTransport;

  public ServiceCombClient(String url) {
    this.url = url;
    httpTransport = DefaultHttpHttpTransport.getInstance();
  }
//
//  public static ServiceCombClient.Builder builder() {
//    return new ServiceCombClient.Builder();
//  }

  public String registerMicroservice(Microservice microservice) throws ServiceCombException {
    Response response = null;
    String content = null;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
      content = objectMapper.writeValueAsString(microservice);
      StringEntity stringEntity = new StringEntity(content, "utf-8");
      response = httpTransport.sendPostRequest(buildURI("/registry/microservices"), stringEntity);
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


  public String getMicroserviceID(Microservice microservice) throws ServiceCombException {
    Response response = null;
    try {
      response = httpTransport.sendGetRequest(buildURI("/registry/existence", "microservice", microservice));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper objectMapper = new ObjectMapper();
        LOGGER.info(response.getContent());
        HashMap<String, String> result = objectMapper.readValue(response.getContent(), HashMap.class);
        return result.get("serviceId");
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


  public String registerInstance(MicroserviceInstance microserviceInstance)
      throws ServiceCombException {
    Response response = null;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
      String content = objectMapper.writeValueAsString(microserviceInstance);
      StringEntity stringEntity = new StringEntity(content, "utf-8");
      response = httpTransport
          .sendPostRequest(buildURI("/registry/microservices/" + microserviceInstance.getServiceId() + "/instances"),
              stringEntity);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        LOGGER.info(response.getContent());
        HashMap<String, String> result = objectMapper.readValue(response.getContent(), HashMap.class);
        return result.get("instanceId");
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

  public List<ServiceInstance> getInstances(Microservice microservice)
      throws ServiceCombException {
    List<ServiceInstance> instanceList = new ArrayList<>();
    Response response = null;
    try {
      response = httpTransport.sendGetRequest(buildURI("/registry/instances", null, microservice));
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        LOGGER.info(response.getContent());
        Microservice result = objectMapper.readValue(response.getContent(), Microservice.class);
        for (MicroserviceInstance instance : result.getInstances()) {
          int port;
          if (!instance.getEndpoints().isEmpty()) {
            String endpoint = instance.getEndpoints().get(0);
            URIBuilder endpointURIBuilder = new URIBuilder(endpoint);
            port = endpointURIBuilder.build().getPort();
          } else {
            throw new RemoteOperationException(
                "read response failed. status=" + response.getStatusCode() + ";mesage=" + response.getStatusMessage());
          }
          //instance.getHostName() TODO
          instanceList.add(
              new DefaultServiceInstance(instance.getInstanceId(), instance.getServiceId(), "127.0.0.1", port, false));
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

  public void heartbeat(HeartbeatRequest heartbeatRequest) throws ServiceCombException {
    Response response = null;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String content = objectMapper.writeValueAsString(heartbeatRequest);
      StringEntity stringEntity = new StringEntity(content, "utf-8");
      response = httpTransport.sendPutRequest(buildURI("/registry/heartbeats"), stringEntity);
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

  private String buildURI(String path) throws URISyntaxException {
    URIBuilder uriBuilder = new URIBuilder(url + path);
    return uriBuilder.build().toString();
  }

  private String buildURI(String path, String type, Microservice microservice) throws URISyntaxException {
    URIBuilder uriBuilder = new URIBuilder(url + path);
    if (null != type) {
      uriBuilder.setParameter("type", "microservice");
    }
    uriBuilder.setParameter("appId", microservice.getAppId());
    uriBuilder.setParameter("serviceName", microservice.getServiceName());
    uriBuilder.setParameter("version", microservice.getVersion());
    return uriBuilder.build().toString();
  }


  private void handleRemoteOperationException(Response response, IOException e) throws RemoteOperationException {
    String message = "read response failed. ";
    if (null != response) {
      message = message + response;
    }
    throw new RemoteOperationException(message, e);
  }

//
//  public static final class Builder {
//
//    private String address;
//
//    private Builder() {
//    }
//    public ServiceCombClient build() {
//      String url = address + "/" + ServiceRegistryConfig.DEFAULT_API_VERSION + "/"
//          + ServiceRegistryConfig.DEFAULT_PROJECT;
//      return new ServiceCombClientBuilder().setUrl(url).createServiceCombClient();
//    }
//
//    public ServiceCombClient.Builder setAddress(String address) {
//      this.address = address;
//      return this;
//    }
//  }
}