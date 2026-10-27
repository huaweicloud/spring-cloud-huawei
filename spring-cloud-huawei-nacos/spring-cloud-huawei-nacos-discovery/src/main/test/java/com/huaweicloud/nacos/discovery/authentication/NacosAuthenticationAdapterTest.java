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

package com.huaweicloud.nacos.discovery.authentication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;

import com.huaweicloud.governance.GovernanceConst;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;
import com.huaweicloud.nacos.discovery.registry.NacosRegistration;

public class NacosAuthenticationAdapterTest {

  private DiscoveryClient discoveryClient;

  private NacosAuthenticationAdapter adapter;

  @BeforeEach
  public void setUp() {
    discoveryClient = Mockito.mock(DiscoveryClient.class);
    adapter = new NacosAuthenticationAdapter(discoveryClient);
  }

  // Test scenario: getInstanceId returns the registration's instanceId when it is non-empty
  @Test
  public void getInstanceIdReturnsRegistrationInstanceIdWhenPresent() {
    Registration registration = Mockito.mock(Registration.class);
    Mockito.when(registration.getInstanceId()).thenReturn("inst-001");

    String result = adapter.getInstanceId(registration);

    Assertions.assertEquals("inst-001", result,
        "getInstanceId should return the registration's instanceId when it is non-empty");
  }

  // Test scenario: getInstanceId computes id from host and port when instanceId is empty
  @Test
  public void getInstanceIdComputesFromHostAndPortWhenInstanceIdEmpty() {
    Registration registration = Mockito.mock(Registration.class);
    Mockito.when(registration.getInstanceId()).thenReturn(null);
    Mockito.when(registration.getHost()).thenReturn("10.0.0.1");
    Mockito.when(registration.getPort()).thenReturn(8080);

    String result = adapter.getInstanceId(registration);

    Assertions.assertEquals("10-0-0-1-8080", result,
        "getInstanceId should compute id from host:port when instanceId is empty");
  }

  // Test scenario: getServiceId returns the registration's serviceId directly
  @Test
  public void getServiceIdReturnsRegistrationServiceId() {
    Registration registration = Mockito.mock(Registration.class);
    Mockito.when(registration.getServiceId()).thenReturn("my-service");

    String result = adapter.getServiceId(registration);

    Assertions.assertEquals("my-service", result,
        "getServiceId should return the registration's serviceId directly");
  }

  // Test scenario: setPublicKey stores the public key into the registration metadata under INSTANCE_PUBKEY_PRO
  @Test
  public void setPublicKeyStoresPublicKeyIntoMetadata() {
    NacosDiscoveryProperties properties = new NacosDiscoveryProperties();
    NacosRegistration registration = new NacosRegistration(List.of(), properties);

    adapter.setPublicKey(registration, "my-public-key");

    Assertions.assertEquals("my-public-key",
        registration.getMetadata().get(GovernanceConst.INSTANCE_PUBKEY_PRO),
        "setPublicKey should store the public key into metadata under INSTANCE_PUBKEY_PRO");
  }

  // Test scenario: getPublicKeyFromInstance returns the public key when the matching instance is found
  @Test
  public void getPublicKeyFromInstanceReturnsPublicKeyWhenInstanceFound() {
    ServiceInstance instance = Mockito.mock(ServiceInstance.class);
    Mockito.when(instance.getInstanceId()).thenReturn("pubkey-inst-001");
    Map<String, String> metadata = new HashMap<>();
    metadata.put(GovernanceConst.INSTANCE_PUBKEY_PRO, "rsa-key-001");
    Mockito.when(instance.getMetadata()).thenReturn(metadata);

    Mockito.when(discoveryClient.getInstances("pubkey-service"))
        .thenReturn(List.of(instance));

    String result = adapter.getPublicKeyFromInstance("pubkey-inst-001", "pubkey-service");

    Assertions.assertEquals("rsa-key-001", result,
        "getPublicKeyFromInstance should return the public key when the matching instance is found");
  }

  // Test scenario: getPropertyValue returns the serviceId when propertyName equals "serviceName"
  @Test
  public void getPropertyValueReturnsServiceIdWhenPropertyNameIsServiceName() {
    ServiceInstance instance = Mockito.mock(ServiceInstance.class);
    Mockito.when(instance.getInstanceId()).thenReturn("svcname-inst-001");
    Mockito.when(instance.getServiceId()).thenReturn("svcname-service");
    Mockito.when(instance.getMetadata()).thenReturn(new HashMap<>());

    Mockito.when(discoveryClient.getInstances("svcname-service"))
        .thenReturn(List.of(instance));

    String result = adapter.getPropertyValue("svcname-service", "svcname-inst-001",
        NacosAuthenticationAdapter.SERVICE_NAME);

    Assertions.assertEquals("svcname-service", result,
        "getPropertyValue should return serviceId when propertyName equals serviceName");
  }

  // Test scenario: getPropertyValue returns the metadata value when instance is found and propertyName is a metadata key
  @Test
  public void getPropertyValueReturnsMetadataValueWhenInstanceFound() {
    ServiceInstance instance = Mockito.mock(ServiceInstance.class);
    Mockito.when(instance.getInstanceId()).thenReturn("meta-inst-001");
    Map<String, String> metadata = new HashMap<>();
    metadata.put("custom-key", "custom-value");
    Mockito.when(instance.getMetadata()).thenReturn(metadata);

    Mockito.when(discoveryClient.getInstances("meta-service"))
        .thenReturn(List.of(instance));

    String result = adapter.getPropertyValue("meta-service", "meta-inst-001", "custom-key");

    Assertions.assertEquals("custom-value", result,
        "getPropertyValue should return the metadata value when instance is found");
  }

  // Test scenario: getPropertyValue returns empty string when no matching instance is found among discovered instances
  @Test
  public void getPropertyValueReturnsEmptyWhenInstanceNotFound() {
    ServiceInstance instance = Mockito.mock(ServiceInstance.class);
    Mockito.when(instance.getInstanceId()).thenReturn("other-inst");

    Mockito.when(discoveryClient.getInstances("notfound-service"))
        .thenReturn(List.of(instance));

    String result = adapter.getPropertyValue("notfound-service", "target-inst", "custom-key");

    Assertions.assertEquals("", result,
        "getPropertyValue should return empty string when no matching instance is found");
  }

  // Test scenario: getPropertyValue returns empty string when discovery client returns an empty instance list
  @Test
  public void getPropertyValueReturnsEmptyWhenDiscoveryReturnsEmptyList() {
    Mockito.when(discoveryClient.getInstances("empty-service"))
        .thenReturn(List.of());

    String result = adapter.getPropertyValue("empty-service", "any-inst", "custom-key");

    Assertions.assertEquals("", result,
        "getPropertyValue should return empty string when discovery returns empty list");
  }

  // Test scenario: getPropertyValue caches the service instance and does not re-query discovery on subsequent calls
  @Test
  public void getPropertyValueCachesServiceInstanceAcrossCalls() {
    ServiceInstance instance = Mockito.mock(ServiceInstance.class);
    Mockito.when(instance.getInstanceId()).thenReturn("cache-inst-001");
    Mockito.when(instance.getServiceId()).thenReturn("cache-service");
    Mockito.when(instance.getMetadata()).thenReturn(new HashMap<>());

    Mockito.when(discoveryClient.getInstances("cache-service"))
        .thenReturn(List.of(instance));

    // First call populates the cache
    adapter.getPropertyValue("cache-service", "cache-inst-001",
        NacosAuthenticationAdapter.SERVICE_NAME);
    // Second call should use the cached value without re-querying discovery
    String result = adapter.getPropertyValue("cache-service", "cache-inst-001",
        NacosAuthenticationAdapter.SERVICE_NAME);

    Assertions.assertEquals("cache-service", result,
        "cached call should return the same serviceId without re-querying discovery");
    Mockito.verify(discoveryClient, Mockito.times(1)).getInstances("cache-service");
  }

  // Test scenario: getServiceName returns the serviceId unchanged
  @Test
  public void getServiceNameReturnsServiceIdUnchanged() {
    String result = adapter.getServiceName("my-service");

    Assertions.assertEquals("my-service", result,
        "getServiceName should return the serviceId unchanged");
  }
}