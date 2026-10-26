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

package com.huaweicloud.nacos.discovery.graceful;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.huaweicloud.nacos.discovery.registry.NacosAutoServiceRegistration;
import com.huaweicloud.nacos.discovery.registry.NacosRegistration;
import com.huaweicloud.nacos.discovery.registry.NacosServiceRegistry;

public class NacosGracefulEndpointTest {

  private NacosServiceRegistry nacosServiceRegistry;

  private NacosRegistration nacosRegistration;

  private NacosAutoServiceRegistration nacosAutoServiceRegistration;

  private NacosGracefulEndpoint endpoint;

  @BeforeEach
  public void setUp() {
    nacosServiceRegistry = Mockito.mock(NacosServiceRegistry.class);
    nacosRegistration = Mockito.mock(NacosRegistration.class);
    nacosAutoServiceRegistration = Mockito.mock(NacosAutoServiceRegistration.class);
    endpoint = new NacosGracefulEndpoint(nacosServiceRegistry, nacosRegistration,
        nacosAutoServiceRegistration);
  }

  // Test scenario: enables registration and triggers registryExtend when status is UP and registration is currently disabled
  @Test
  public void gracefulUpperAndDownUpStatusWhenDisabled() {
    Mockito.when(nacosAutoServiceRegistration.isEnabled()).thenReturn(false);

    endpoint.gracefulUpperAndDown("UP");

    Mockito.verify(nacosAutoServiceRegistration).setRegistryEnabled(true);
    Mockito.verify(nacosAutoServiceRegistration).registryExtend();
    Mockito.verify(nacosServiceRegistry, Mockito.never())
        .deregister(Mockito.any(NacosRegistration.class));
  }

  // Test scenario: deregisters and disables registration when status is DOWN and registration is currently enabled
  @Test
  public void gracefulUpperAndDownDownStatusWhenEnabled() {
    Mockito.when(nacosAutoServiceRegistration.isEnabled()).thenReturn(true);

    endpoint.gracefulUpperAndDown("DOWN");

    Mockito.verify(nacosServiceRegistry).deregister(nacosRegistration);
    Mockito.verify(nacosAutoServiceRegistration).setRegistryEnabled(false);
    Mockito.verify(nacosAutoServiceRegistration, Mockito.never()).registryExtend();
  }

  // Test scenario: does not perform any operation when status is UP but registration is already enabled
  @Test
  public void gracefulUpperAndDownUpStatusWhenAlreadyEnabled() {
    Mockito.when(nacosAutoServiceRegistration.isEnabled()).thenReturn(true);

    endpoint.gracefulUpperAndDown("UP");

    Mockito.verify(nacosAutoServiceRegistration, Mockito.never())
        .setRegistryEnabled(Mockito.anyBoolean());
    Mockito.verify(nacosAutoServiceRegistration, Mockito.never()).registryExtend();
    Mockito.verify(nacosServiceRegistry, Mockito.never())
        .deregister(Mockito.any(NacosRegistration.class));
  }

  // Test scenario: does not perform any operation when status is DOWN but registration is already disabled
  @Test
  public void gracefulUpperAndDownDownStatusWhenAlreadyDisabled() {
    Mockito.when(nacosAutoServiceRegistration.isEnabled()).thenReturn(false);

    endpoint.gracefulUpperAndDown("DOWN");

    Mockito.verify(nacosAutoServiceRegistration, Mockito.never())
        .setRegistryEnabled(Mockito.anyBoolean());
    Mockito.verify(nacosAutoServiceRegistration, Mockito.never()).registryExtend();
    Mockito.verify(nacosServiceRegistry, Mockito.never())
        .deregister(Mockito.any(NacosRegistration.class));
  }

  // Test scenario: does not perform any operation when status is an unrecognized value
  @Test
  public void gracefulUpperAndDownInvalidStatus() {
    Mockito.when(nacosAutoServiceRegistration.isEnabled()).thenReturn(true);

    endpoint.gracefulUpperAndDown("INVALID");

    Mockito.verify(nacosAutoServiceRegistration, Mockito.never())
        .setRegistryEnabled(Mockito.anyBoolean());
    Mockito.verify(nacosAutoServiceRegistration, Mockito.never()).registryExtend();
    Mockito.verify(nacosServiceRegistry, Mockito.never())
        .deregister(Mockito.any(NacosRegistration.class));
  }

  // Test scenario: does not perform any operation when status is null
  @Test
  public void gracefulUpperAndDownNullStatus() {
    Mockito.when(nacosAutoServiceRegistration.isEnabled()).thenReturn(true);

    endpoint.gracefulUpperAndDown(null);

    Mockito.verify(nacosAutoServiceRegistration, Mockito.never())
        .setRegistryEnabled(Mockito.anyBoolean());
    Mockito.verify(nacosAutoServiceRegistration, Mockito.never()).registryExtend();
    Mockito.verify(nacosServiceRegistry, Mockito.never())
        .deregister(Mockito.any(NacosRegistration.class));
  }

  // Test scenario: enables registration when status is lowercase "up" (case-insensitive matching)
  @Test
  public void gracefulUpperAndDownUpStatusCaseInsensitive() {
    Mockito.when(nacosAutoServiceRegistration.isEnabled()).thenReturn(false);

    endpoint.gracefulUpperAndDown("up");

    Mockito.verify(nacosAutoServiceRegistration).setRegistryEnabled(true);
    Mockito.verify(nacosAutoServiceRegistration).registryExtend();
    Mockito.verify(nacosServiceRegistry, Mockito.never())
        .deregister(Mockito.any(NacosRegistration.class));
  }

  // Test scenario: deregisters and disables when status is lowercase "down" (case-insensitive matching)
  @Test
  public void gracefulUpperAndDownDownStatusCaseInsensitive() {
    Mockito.when(nacosAutoServiceRegistration.isEnabled()).thenReturn(true);

    endpoint.gracefulUpperAndDown("down");

    Mockito.verify(nacosServiceRegistry).deregister(nacosRegistration);
    Mockito.verify(nacosAutoServiceRegistration).setRegistryEnabled(false);
    Mockito.verify(nacosAutoServiceRegistration, Mockito.never()).registryExtend();
  }
}