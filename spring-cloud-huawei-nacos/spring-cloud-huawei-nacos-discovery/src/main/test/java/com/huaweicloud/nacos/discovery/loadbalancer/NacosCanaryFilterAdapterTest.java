/*

 * Copyright (C) 2020-2026 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.nacos.discovery.loadbalancer;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.ServiceInstance;

import com.huaweicloud.nacos.discovery.NacosServiceInstance;

public class NacosCanaryFilterAdapterTest {
  private final NacosCanaryFilterAdapter adapter = new NacosCanaryFilterAdapter();
  @Test
  public void testGetVersion() {
    NacosServiceInstance instance = new NacosServiceInstance();
    instance.setMetadata(new HashMap<>());
    String version = adapter.getVersion(instance);
    Assertions.assertEquals("0.0.1", version);

    version = adapter.getVersion(buildServiceInstance());
    Assertions.assertEquals("0.0.2", version);
  }

  @Test
  public void testGetServiceName() {
    String serviceName = adapter.getServiceName(buildServiceInstance());
    Assertions.assertEquals("test", serviceName);
  }

  @Test
  public void testGetProperties() {
    Map<String, String> map = adapter.getProperties(buildServiceInstance());
    Assertions.assertEquals(2, map.size());
  }

  private ServiceInstance buildServiceInstance() {
    NacosServiceInstance instance = new NacosServiceInstance();
    instance.setServiceId("test");
    Map<String, String> metadata = new HashMap<>();
    metadata.put("region", "west2");
    metadata.put("version", "0.0.2");
    instance.setMetadata(metadata);
    return instance;
  }
}
