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

package com.huaweicloud.servicecomb.discovery.discovery;

import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstance;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class MicroserviceCache {

  private static Map<String, MicroserviceInstance> microserviceList = new ConcurrentHashMap<>();

  public static void initInsList(List<MicroserviceInstance> list, String serviceName) {
    microserviceList.clear();
    list.forEach(ins -> {
      ins.setServiceName(serviceName);
      ins.getEndpoints()
          .forEach(ep -> {
            URI uri = URI.create(ep);
            if (!StringUtils.isEmpty(uri.getAuthority())) {
              microserviceList.put(uri.getAuthority(), ins);
            }
          });
    });
  }

  public static MicroserviceInstance getMicroserviceIns(String insId) {
    return microserviceList.get(insId);
  }
}
