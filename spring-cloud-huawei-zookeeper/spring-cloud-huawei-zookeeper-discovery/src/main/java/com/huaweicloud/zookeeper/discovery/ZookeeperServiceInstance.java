/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.zookeeper.discovery;

import java.net.URI;
import java.util.Map;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

public class ZookeeperServiceInstance implements ServiceInstance {

  private String serviceId;

  private String instanceId;

  private String host;

  private int port;

  private boolean secure;

  private Map<String, String> metadata;

  private String status;

  @Override
  public String getServiceId() {
    return serviceId;
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public boolean isSecure() {
    return secure;
  }

  @Override
  public URI getUri() {
    return DefaultServiceInstance.getUri(this);
  }

  @Override
  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
