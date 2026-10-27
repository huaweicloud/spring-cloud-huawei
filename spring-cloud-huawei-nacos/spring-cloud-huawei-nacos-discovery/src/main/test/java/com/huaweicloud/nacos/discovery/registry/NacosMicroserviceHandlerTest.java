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

package com.huaweicloud.nacos.discovery.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.alibaba.nacos.api.naming.PreservedMetadataKeys;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;

public class NacosMicroserviceHandlerTest {

  private NacosDiscoveryProperties properties;

  private Environment environment;

  @BeforeEach
  public void setUp() {
    properties = new NacosDiscoveryProperties();
    properties.setMetadata(new HashMap<>());
    properties.setIp("127.0.0.1");
    properties.setPort(8080);
    properties.setClusterName("DEFAULT");
    properties.setWeight(2.0f);
    properties.setInstanceEnabled(false);
    properties.setEphemeral(false);
    environment = Mockito.mock(Environment.class);
  }

  // Test scenario: parseArrayValue returns an empty list when input is null
  @Test
  public void parseArrayValueReturnsEmptyListWhenNullInput() {
    List<String> result = NacosMicroserviceHandler.parseArrayValue(null);

    Assertions.assertTrue(result.isEmpty());
  }

  // Test scenario: parseArrayValue trims whitespace from each comma-separated token
  @Test
  public void parseArrayValueTrimsWhitespaceFromTokens() {
    List<String> result = NacosMicroserviceHandler.parseArrayValue(" a , b , c ");

    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals("a", result.get(0));
    Assertions.assertEquals("b", result.get(1));
    Assertions.assertEquals("c", result.get(2));
  }

  // Test scenario: parseArrayValue returns a single trimmed value for a non-comma input
  @Test
  public void parseArrayValueReturnsSingleTrimmedValue() {
    List<String> result = NacosMicroserviceHandler.parseArrayValue("  hello  ");

    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals("hello", result.get(0));
  }

  // Test scenario: createMicroserviceInstance populates basic instance attributes from properties
  @Test
  public void createMicroserviceInstancePopulatesBasicAttributes() {
    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Assertions.assertEquals(2.0, instance.getWeight(), 0.0001);
    Assertions.assertFalse(instance.isEnabled());
    Assertions.assertEquals("DEFAULT", instance.getClusterName());
    Assertions.assertFalse(instance.isEphemeral());
    Assertions.assertEquals(8080, instance.getPort());
    Assertions.assertEquals("127.0.0.1", instance.getIp());
  }

  // Test scenario: createMicroserviceInstance adds framework type metadata key with implementation version
  @Test
  public void createMicroserviceInstanceAddsFrameworkTypeMetadataKey() {
    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Assertions.assertTrue(instance.getMetadata().containsKey("springCloudHuawei"));
  }

  // Test scenario: createMicroserviceInstance adds secure metadata when properties.isSecure is true
  @Test
  public void createMicroserviceInstanceAddsSecureMetadataWhenSecure() {
    properties.setSecure(true);

    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Assertions.assertEquals("true", instance.getMetadata().get("secure"));
  }

  // Test scenario: createMicroserviceInstance does not add secure metadata when properties.isSecure is false
  @Test
  public void createMicroserviceInstanceDoesNotAddSecureMetadataWhenNotSecure() {
    properties.setSecure(false);

    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Assertions.assertFalse(instance.getMetadata().containsKey("secure"));
  }

  // Test scenario: createMicroserviceInstance adds heartbeat metadata when heartbeat values are set
  @Test
  public void createMicroserviceInstanceAddsHeartBeatConfigurationWhenSet() {
    properties.setHeartBeatInterval(1000);
    properties.setHeartBeatTimeout(3000);
    properties.setIpDeleteTimeout(5000);

    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Map<String, String> metadata = instance.getMetadata();
    Assertions.assertEquals("1000", metadata.get(PreservedMetadataKeys.HEART_BEAT_INTERVAL));
    Assertions.assertEquals("3000", metadata.get(PreservedMetadataKeys.HEART_BEAT_TIMEOUT));
    Assertions.assertEquals("5000", metadata.get(PreservedMetadataKeys.IP_DELETE_TIMEOUT));
  }

  // Test scenario: createMicroserviceInstance omits heartbeat metadata when heartbeat values are null
  @Test
  public void createMicroserviceInstanceOmitsHeartBeatConfigurationWhenNull() {
    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Map<String, String> metadata = instance.getMetadata();
    Assertions.assertFalse(metadata.containsKey(PreservedMetadataKeys.HEART_BEAT_INTERVAL));
    Assertions.assertFalse(metadata.containsKey(PreservedMetadataKeys.HEART_BEAT_TIMEOUT));
    Assertions.assertFalse(metadata.containsKey(PreservedMetadataKeys.IP_DELETE_TIMEOUT));
  }

  // Test scenario: createMicroserviceInstance adds CAS properties when environment provides them
  @Test
  public void createMicroserviceInstanceAddsCasPropertiesWhenProvided() {
    Mockito.when(environment.getProperty("CAS_APPLICATION_ID")).thenReturn("app-1");
    Mockito.when(environment.getProperty("CAS_COMPONENT_NAME")).thenReturn("comp-1");
    Mockito.when(environment.getProperty("CAS_INSTANCE_VERSION")).thenReturn("v1");
    Mockito.when(environment.getProperty("CAS_INSTANCE_ID")).thenReturn("inst-1");
    Mockito.when(environment.getProperty("CAS_ENVIRONMENT_ID")).thenReturn("env-1");

    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Map<String, String> metadata = instance.getMetadata();
    Assertions.assertEquals("app-1", metadata.get("CAS_APPLICATION_ID"));
    Assertions.assertEquals("comp-1", metadata.get("CAS_COMPONENT_NAME"));
    Assertions.assertEquals("v1", metadata.get("CAS_INSTANCE_VERSION"));
    Assertions.assertEquals("inst-1", metadata.get("CAS_INSTANCE_ID"));
    Assertions.assertEquals("env-1", metadata.get("CAS_ENVIRONMENT_ID"));
  }

  // Test scenario: createMicroserviceInstance omits CAS properties when environment values are absent
  @Test
  public void createMicroserviceInstanceOmitsCasPropertiesWhenAbsent() {
    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Map<String, String> metadata = instance.getMetadata();
    Assertions.assertFalse(metadata.containsKey("CAS_APPLICATION_ID"));
    Assertions.assertFalse(metadata.containsKey("CAS_COMPONENT_NAME"));
    Assertions.assertFalse(metadata.containsKey("CAS_INSTANCE_VERSION"));
    Assertions.assertFalse(metadata.containsKey("CAS_INSTANCE_ID"));
    Assertions.assertFalse(metadata.containsKey("CAS_ENVIRONMENT_ID"));
    Assertions.assertFalse(metadata.containsKey("version"));
  }

  // Test scenario: createMicroserviceInstance adds version metadata when VERSION_MAPPING resolves to a non-empty value
  @Test
  public void createMicroserviceInstanceAddsVersionWhenMappingResolves() {
    Mockito.when(environment.getProperty("VERSION_MAPPING")).thenReturn("version.key");
    Mockito.when(environment.getProperty("version.key")).thenReturn("1.0.0");

    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Assertions.assertEquals("1.0.0", instance.getMetadata().get("version"));
  }

  // Test scenario: createMicroserviceInstance omits version metadata when mapped value is empty
  @Test
  public void createMicroserviceInstanceOmitsVersionWhenMappedValueEmpty() {
    Mockito.when(environment.getProperty("VERSION_MAPPING")).thenReturn("version.key");
    Mockito.when(environment.getProperty("version.key")).thenReturn("");

    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Assertions.assertFalse(instance.getMetadata().containsKey("version"));
  }

  // Test scenario: createMicroserviceInstance omits version metadata when VERSION_MAPPING itself is absent
  @Test
  public void createMicroserviceInstanceOmitsVersionWhenMappingAbsent() {
    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Assertions.assertFalse(instance.getMetadata().containsKey("version"));
  }

  // Test scenario: createMicroserviceInstance adds instance props parsed from INSTANCE_PROPS
  @Test
  public void createMicroserviceInstanceAddsInstancePropsFromEnvironment() {
    Mockito.when(environment.getProperty("SERVICECOMB_INSTANCE_PROPS")).thenReturn("key1:val1, key2:val2");

    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Map<String, String> metadata = instance.getMetadata();
    Assertions.assertEquals("val1", metadata.get("key1"));
    Assertions.assertEquals("val2", metadata.get("key2"));
  }

  // Test scenario: createMicroserviceInstance filters out malformed instance props entries (not exactly key:value)
  @Test
  public void createMicroserviceInstanceFiltersMalformedInstancePropsEntries() {
    Mockito.when(environment.getProperty("SERVICECOMB_INSTANCE_PROPS")).thenReturn("good:yes, malformed, a:b:c");

    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Map<String, String> metadata = instance.getMetadata();
    Assertions.assertEquals("yes", metadata.get("good"));
    Assertions.assertFalse(metadata.containsKey("malformed"));
    Assertions.assertFalse(metadata.containsKey("a"));
  }

  // Test scenario: createMicroserviceInstance mutates the properties metadata map in place
  @Test
  public void createMicroserviceInstanceMutatesPropertiesMetadataInPlace() {
    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(properties, environment);

    Assertions.assertSame(instance.getMetadata(), properties.getMetadata());
    Assertions.assertTrue(properties.getMetadata().containsKey("springCloudHuawei"));
  }

  // Test scenario: createMicroserviceMetadata returns parsed props when SERVICE_PROPS is provided
  @Test
  public void createMicroserviceMetadataReturnsParsedPropsWhenProvided() {
    Mockito.when(environment.getProperty("SERVICECOMB_SERVICE_PROPS")).thenReturn("prop1:val1, prop2:val2");

    Map<String, String> result = NacosMicroserviceHandler.createMicroserviceMetadata(environment);

    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals("val1", result.get("prop1"));
    Assertions.assertEquals("val2", result.get("prop2"));
  }

  // Test scenario: createMicroserviceMetadata filters malformed entries from SERVICE_PROPS
  @Test
  public void createMicroserviceMetadataFiltersMalformedEntries() {
    Mockito.when(environment.getProperty("SERVICECOMB_SERVICE_PROPS")).thenReturn("good:yes, bad, x:y:z");

    Map<String, String> result = NacosMicroserviceHandler.createMicroserviceMetadata(environment);

    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals("yes", result.get("good"));
    Assertions.assertFalse(result.containsKey("bad"));
    Assertions.assertFalse(result.containsKey("x"));
  }

  // Test scenario: createMicroserviceMetadata returns empty map when SERVICE_PROPS is null
  @Test
  public void createMicroserviceMetadataReturnsEmptyMapWhenNull() {
    Map<String, String> result = NacosMicroserviceHandler.createMicroserviceMetadata(environment);

    Assertions.assertTrue(result.isEmpty());
    Assertions.assertEquals(Collections.emptyMap(), result);
  }
}