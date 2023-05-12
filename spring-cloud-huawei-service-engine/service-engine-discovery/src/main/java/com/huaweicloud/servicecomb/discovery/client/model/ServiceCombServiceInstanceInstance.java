/*

 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.servicecomb.discovery.client.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.springframework.cloud.client.ServiceInstance;

import com.huaweicloud.common.governance.GovernaceServiceInstance;

public class ServiceCombServiceInstanceInstance implements ServiceInstance, GovernaceServiceInstance {
  private final URIEndpointObject uriEndpointObject;

  private final MicroserviceInstance microserviceInstance;

  public ServiceCombServiceInstanceInstance(MicroserviceInstance microserviceInstance) {
    this.microserviceInstance = microserviceInstance;
    String endpoint = this.microserviceInstance.getEndpoints().stream().filter(e -> e.startsWith("rest://"))
        .findFirst().orElse(null);
    if (endpoint != null) {
      this.uriEndpointObject = new URIEndpointObject(endpoint);
    } else {
      this.uriEndpointObject = null;
    }
  }

  @Override
  public void setPublickey(String publicKeyEncoded) {

  }

  @Override
  public String getInstanceId() {
    return this.microserviceInstance.getInstanceId();
  }

  @Override
  public String getVersion(ServiceInstance serviceInstance) {
    return this.microserviceInstance.getVersion();
  }

  @Override
  public String getServiceName(ServiceInstance serviceInstance) {
    return this.microserviceInstance.getServiceName();
  }

  @Override
  public Map<String, String> getProperties(ServiceInstance serviceInstance) {
    return this.microserviceInstance.getProperties();
  }

  @Override
  public String getAvailableZone() {
    if (microserviceInstance.getDataCenterInfo() != null) {
      return microserviceInstance.getDataCenterInfo().getAvailableZone();
    }
    return null;
  }

  @Override
  public String getRegion() {
    if (microserviceInstance.getDataCenterInfo() != null) {
      return microserviceInstance.getDataCenterInfo().getRegion();
    }
    return null;
  }

  @Override
  public String getServiceId() {
    return this.microserviceInstance.getServiceName();
  }

  @Override
  public String getPublicKeyFromInstance(String instanceId, String serviceId) {
    return null;
  }

  @Override
  public String getPropertyValue(String serviceId, String instanceId, String propertyName) {
    return null;
  }

  @Override
  public String getHost() {
    if (uriEndpointObject == null) {
      return this.microserviceInstance.getInstanceId(); // compatible to ribbon default host name
    }
    return uriEndpointObject.getHostOrIp();
  }

  @Override
  public int getPort() {
    if (uriEndpointObject == null) {
      return 0;
    }
    return uriEndpointObject.getPort();
  }

  @Override
  public boolean isSecure() {
    if (uriEndpointObject == null) {
      return false;
    }
    return uriEndpointObject.isSslEnabled();
  }

  @Override
  public URI getUri() {
    String scheme = this.getScheme();
    String uri = String.format("%s://%s:%s", scheme, uriEndpointObject.getHostOrIp(), uriEndpointObject.getPort());
    return URI.create(uri);
  }

  @Override
  public Map<String, String> getMetadata() {
    Map<String, String> map = new HashMap<>();
    map.putAll(this.microserviceInstance.getProperties());
    if (this.microserviceInstance.getStatus() != null) {
      map.put(DiscoveryConstants.INSTANCE_STATUS, this.microserviceInstance.getStatus().name());
    }
    if (this.microserviceInstance.getDataCenterInfo() != null) {
      map.put(DiscoveryConstants.INSTANCE_ZONE, this.microserviceInstance.getDataCenterInfo().getAvailableZone());
    }
    return map;
  }

  @Override
  public String getScheme() {
    if (uriEndpointObject == null) {
      return "http";
    }
    return uriEndpointObject.isSslEnabled() ? "https" : "http";
  }
}
