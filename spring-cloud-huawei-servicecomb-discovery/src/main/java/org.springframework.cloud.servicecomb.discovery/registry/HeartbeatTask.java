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

package org.springframework.cloud.servicecomb.discovery.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicecomb.discovery.client.ServiceCombClient;
import org.springframework.cloud.servicecomb.discovery.client.exception.ServiceCombException;
import org.springframework.cloud.servicecomb.discovery.client.model.HeartbeatRequest;

/**
 * @Author wangqijun
 * @Date 11:13 2019-07-15
 **/

public class HeartbeatTask implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatTask.class);

  private HeartbeatRequest heartbeatRequest;

  private ServiceCombClient serviceCombClient;

  public HeartbeatTask(HeartbeatRequest heartbeatRequest, ServiceCombClient serviceCombClient) {
    this.heartbeatRequest = heartbeatRequest;
    this.serviceCombClient = serviceCombClient;
  }

  @Override
  public void run() {
    try {
      serviceCombClient.heartbeat(heartbeatRequest);
    } catch (ServiceCombException e) {
      LOGGER.warn("heartbeat failed.", e);
    }
  }
}
