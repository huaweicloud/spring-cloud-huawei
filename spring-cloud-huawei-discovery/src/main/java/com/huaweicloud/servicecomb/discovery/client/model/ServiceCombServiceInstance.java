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

package com.huaweicloud.servicecomb.discovery.client.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;

public class ServiceCombServiceInstance implements ServiceInstance {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombServiceInstance.class);

  private final MicroserviceInstance microserviceInstance;

  public ServiceCombServiceInstance(MicroserviceInstance microserviceInstance) {
    this.microserviceInstance = microserviceInstance;
  }

  public MicroserviceInstance getMicroserviceInstance() {
    return this.microserviceInstance;
  }

  @Override
  public String getServiceId() {
    return this.microserviceInstance.getServiceName();
  }

  @Override
  public String getHost() {
    URI uri = parseEndpoint();
    if (uri == null) {
      return this.microserviceInstance.getInstanceId(); // compatible to ribbon default host name
    }
    return uri.getHost();
  }

  private URI parseEndpoint() {
    String endpoint = this.microserviceInstance.getEndpoints().stream().filter(e -> e.startsWith("rest://"))
        .findFirst().orElse(null);

    if (endpoint == null) {
      return null;
    }

    URI uri = null;
    try {
      uri = new URIBuilder(endpoint).build();
    } catch (URISyntaxException e) {
      LOGGER.error("invalid instance endpoint [{}]", endpoint);
    }
    return uri;
  }

  @Override
  public int getPort() {
    URI uri = parseEndpoint();
    if (uri == null) {
      return 0;
    }
    return uri.getPort();
  }

  @Override
  public boolean isSecure() {
    // TODO: add secure implementation
    return false;
  }

  @Override
  public URI getUri() {
    return parseEndpoint();
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
    // TODO: add schema implementation
    return null;
  }
}
