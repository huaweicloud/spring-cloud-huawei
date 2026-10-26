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
import org.mockito.Mockito;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import com.huaweicloud.nacos.discovery.NacosServiceInstance;

public class NacosZoneAwareFilterAdapterTest {
  private final NacosZoneAwareFilterAdapter adapter = new NacosZoneAwareFilterAdapter();

  @Test
  public void testGetRegion() {
    NacosServiceInstance instance = new NacosServiceInstance();
    instance.setMetadata(new HashMap<>());
    String region = adapter.getRegion(instance);
    Assertions.assertEquals("0.0.1", region);

    region = adapter.getRegion(buildServiceInstance());
    Assertions.assertEquals("west2", region);
  }

  @Test
  public void testGetAvailableZone() {
    NacosServiceInstance instance = new NacosServiceInstance();
    instance.setMetadata(new HashMap<>());
    String zone = adapter.getAvailableZone(instance);
    Assertions.assertEquals("0.0.1", zone);

    zone = adapter.getAvailableZone(buildServiceInstance());
    Assertions.assertEquals("zone1", zone);
  }

  @Test
  public void testGetRegionByRegistration() {
    Registration registration = Mockito.mock(Registration.class);
    Mockito.when(registration.getMetadata()).thenReturn(new HashMap<>());
    String region = adapter.getRegion(registration);
    Assertions.assertEquals("0.0.1", region);

    Mockito.when(registration.getMetadata()).thenReturn(buildMetadata());
    region = adapter.getRegion(registration);
    Assertions.assertEquals("west2", region);
  }

  @Test
  public void testGetAvailableZoneByRegistration() {
    Registration registration = Mockito.mock(Registration.class);
    Mockito.when(registration.getMetadata()).thenReturn(new HashMap<>());
    String zone = adapter.getAvailableZone(registration);
    Assertions.assertEquals("0.0.1", zone);

    Mockito.when(registration.getMetadata()).thenReturn(buildMetadata());
    zone = adapter.getAvailableZone(registration);
    Assertions.assertEquals("zone1", zone);
  }

  private ServiceInstance buildServiceInstance() {
    NacosServiceInstance instance = new NacosServiceInstance();
    Map<String, String> metadata = new HashMap<>();
    metadata.put("region", "west2");
    metadata.put("zone", "zone1");
    instance.setMetadata(metadata);
    return instance;
  }

  private Map<String, String> buildMetadata() {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("region", "west2");
    metadata.put("zone", "zone1");
    return metadata;
  }
}
