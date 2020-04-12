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

package com.huaweicloud.servicecomb.discovery.ribbon;

import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstanceStatus;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.discovery.MicroserviceHandler;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;

/**
 * @Author wangqijun
 * @Date 09:31 2019-07-12
 **/
public class ServiceCombServerList extends AbstractServerList<Server> {

  @Autowired
  private ServiceCombClient serviceCombClient;

  @Autowired
  private ILoadBalancer loadBalancer;

  private ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  private String serviceId;

  public ServiceCombServerList(
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties) {
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
  }

  @Override
  public void initWithNiwsConfig(IClientConfig iClientConfig) {
    this.serviceId = iClientConfig.getClientName();
  }

  @Override
  public List<Server> getInitialListOfServers() {
    return Collections.emptyList();
  }

  @Override
  public List<Server> getUpdatedListOfServers() {
    Microservice microService = MicroserviceHandler
        .createMicroservice(serviceCombDiscoveryProperties, serviceId);
    //spring cloud serviceId equals servicecomb serviceName
    List<ServiceInstance> instanceList = MicroserviceHandler
        .getInstances(microService, serviceCombClient);
    if (instanceList.isEmpty()) {
      return loadBalancer.getAllServers();
    }
    return transform(instanceList);
  }

  private List<Server> transform(List<ServiceInstance> instanceList) {
    List<Server> serverList = new ArrayList<>();
    instanceList.forEach(
        instance -> {
          if (instance
              .getMetadata()
              .get(ServiceCombClient.INSTANCE_STATUS)
              .equals(MicroserviceInstanceStatus.UP.name())) {
            Server server = new Server(instance.getHost(), instance.getPort());
            if (instance.getMetadata().containsKey(ServiceCombClient.ZONE)) {
              server.setZone(instance.getMetadata().get(ServiceCombClient.ZONE));
            }
            serverList.add(server);
          }
        });
    return serverList;
  }
}
