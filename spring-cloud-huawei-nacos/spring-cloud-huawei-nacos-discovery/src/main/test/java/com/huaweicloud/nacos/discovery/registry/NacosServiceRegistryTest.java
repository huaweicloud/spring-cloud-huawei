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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.core.env.Environment;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.eventbus.EventBus;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.nacos.discovery.NacosConst;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;
import com.huaweicloud.nacos.discovery.manager.NamingServiceManager;

public class NacosServiceRegistryTest {

  private NamingServiceManager namingServiceManager;

  private NamingService namingService;

  private NamingMaintainService namingMaintainService;

  private Environment environment;

  private Registration registration;

  private EventBus eventBus;

  private NacosDiscoveryProperties nacosDiscoveryProperties;

  private NacosServiceRegistry serviceRegistry;

  private Instance testInstance;

  private MockedStatic<EventManager> eventManagerMock;

  private MockedStatic<NacosMicroserviceHandler> microserviceHandlerMock;

  @BeforeEach
  public void setUp() {
    namingServiceManager = Mockito.mock(NamingServiceManager.class);
    namingService = Mockito.mock(NamingService.class);
    namingMaintainService = Mockito.mock(NamingMaintainService.class);
    environment = Mockito.mock(Environment.class);
    registration = Mockito.mock(Registration.class);
    eventBus = Mockito.mock(EventBus.class);

    nacosDiscoveryProperties = new NacosDiscoveryProperties();
    nacosDiscoveryProperties.setIp("127.0.0.1");
    nacosDiscoveryProperties.setPort(8080);
    nacosDiscoveryProperties.setGroup("DEFAULT_GROUP");
    nacosDiscoveryProperties.setClusterName("DEFAULT");

    testInstance = new Instance();
    testInstance.setIp("127.0.0.1");
    testInstance.setPort(8080);

    eventManagerMock = Mockito.mockStatic(EventManager.class);
    eventManagerMock.when(EventManager::getEventBus).thenReturn(eventBus);

    microserviceHandlerMock = Mockito.mockStatic(NacosMicroserviceHandler.class);
    microserviceHandlerMock.when(() -> NacosMicroserviceHandler
            .createMicroserviceInstance(Mockito.any(NacosDiscoveryProperties.class), Mockito.any(Environment.class)))
        .thenReturn(testInstance);

    Mockito.when(namingServiceManager.getNamingService()).thenReturn(namingService);
    Mockito.when(namingServiceManager.getNamingMaintainService()).thenReturn(namingMaintainService);
    Mockito.when(namingServiceManager.getServerAddr()).thenReturn("127.0.0.1:8848");
    Mockito.when(namingServiceManager.getOrder()).thenReturn(0);

    Mockito.when(registration.getServiceId()).thenReturn("test-service");
    Mockito.when(registration.getInstanceId()).thenReturn("instance-1");
    Mockito.when(registration.getHost()).thenReturn("127.0.0.1");
    Mockito.when(registration.getPort()).thenReturn(8080);

    serviceRegistry = new NacosServiceRegistry(
        List.of(namingServiceManager), nacosDiscoveryProperties, environment);
  }

  @AfterEach
  public void tearDown() {
    microserviceHandlerMock.close();
    eventManagerMock.close();
  }

  // Test scenario: register succeeds and posts a success event when serviceId is valid and metadata is empty
  @Test
  public void registerSuccessWithEmptyMetadata() throws Exception {
    serviceRegistry.register(registration);

    Mockito.verify(namingService).registerInstance("test-service", "DEFAULT_GROUP", testInstance);
    ArgumentCaptor<NacosServiceRegistrationEvent> captor =
        ArgumentCaptor.forClass(NacosServiceRegistrationEvent.class);
    Mockito.verify(eventBus).post(captor.capture());
    NacosServiceRegistrationEvent event = captor.getValue();
    Assertions.assertTrue(event.isSuccess(), "registration event should indicate success");
    Assertions.assertEquals("instance-1", event.getInstance().getInstanceId(),
        "instanceId should be set from registration");
  }

  // Test scenario: register returns early without posting any event when serviceId is empty
  @Test
  public void registerReturnsEarlyWhenServiceIdIsEmpty() throws Exception {
    Mockito.when(registration.getServiceId()).thenReturn("");

    serviceRegistry.register(registration);

    Mockito.verify(namingService, Mockito.never())
        .registerInstance(Mockito.anyString(), Mockito.anyString(), Mockito.any(Instance.class));
    Mockito.verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  // Test scenario: register posts a failure event when all managers fail to register the instance
  @Test
  public void registerPostsFailureEventWhenAllManagersFail() throws Exception {
    Mockito.doThrow(new NacosException(500, "register failed")).when(namingService)
        .registerInstance(Mockito.anyString(), Mockito.anyString(), Mockito.any(Instance.class));

    serviceRegistry.register(registration);

    ArgumentCaptor<NacosServiceRegistrationEvent> captor =
        ArgumentCaptor.forClass(NacosServiceRegistrationEvent.class);
    Mockito.verify(eventBus).post(captor.capture());
    NacosServiceRegistrationEvent event = captor.getValue();
    Assertions.assertFalse(event.isSuccess(), "registration event should indicate failure when all managers fail");
  }

  // Test scenario: register updates service metadata via tryUpdateService when createMicroserviceMetadata returns non-empty
  @Timeout(10)
  @Test
  public void registerUpdatesServiceMetadataWhenPresent() throws Exception {
    microserviceHandlerMock.when(() -> NacosMicroserviceHandler.createMicroserviceMetadata(Mockito.any()))
        .thenReturn(Map.of("key", "value"));

    serviceRegistry.register(registration);

    Mockito.verify(namingMaintainService)
        .updateService("test-service", "DEFAULT_GROUP", 0, Map.of("key", "value"));
    ArgumentCaptor<NacosServiceRegistrationEvent> captor =
        ArgumentCaptor.forClass(NacosServiceRegistrationEvent.class);
    Mockito.verify(eventBus).post(captor.capture());
    Assertions.assertTrue(captor.getValue().isSuccess());
  }

  // Test scenario: register posts a success event when at least one manager succeeds even if another fails
  @Test
  public void registerSucceedsWithPartialManagerFailure() throws Exception {
    NamingServiceManager secondManager = Mockito.mock(NamingServiceManager.class);
    NamingService secondNamingService = Mockito.mock(NamingService.class);
    NamingMaintainService secondMaintainService = Mockito.mock(NamingMaintainService.class);
    Mockito.when(secondManager.getNamingService()).thenReturn(secondNamingService);
    Mockito.when(secondManager.getNamingMaintainService()).thenReturn(secondMaintainService);
    Mockito.when(secondManager.getServerAddr()).thenReturn("standby:8848");
    Mockito.when(secondManager.getOrder()).thenReturn(1);

    Mockito.doThrow(new NacosException(500, "fail")).when(namingService)
        .registerInstance(Mockito.anyString(), Mockito.anyString(), Mockito.any(Instance.class));

    NacosServiceRegistry registry = new NacosServiceRegistry(
        List.of(namingServiceManager, secondManager), nacosDiscoveryProperties, environment);
    registry.register(registration);

    ArgumentCaptor<NacosServiceRegistrationEvent> captor =
        ArgumentCaptor.forClass(NacosServiceRegistrationEvent.class);
    Mockito.verify(eventBus).post(captor.capture());
    Assertions.assertTrue(captor.getValue().isSuccess());
    Mockito.verify(secondNamingService).registerInstance("test-service", "DEFAULT_GROUP", testInstance);
  }

  // Test scenario: deregister posts a deregistration event when all managers succeed
  @Test
  public void deregisterSuccessPostsEventWhenAllManagersSucceed() throws Exception {
    serviceRegistry.deregister(registration);

    Mockito.verify(namingService).deregisterInstance(
        "test-service", "DEFAULT_GROUP", "127.0.0.1", 8080, "DEFAULT");
    ArgumentCaptor<NacosServiceRegistrationEvent> captor =
        ArgumentCaptor.forClass(NacosServiceRegistrationEvent.class);
    Mockito.verify(eventBus).post(captor.capture());
    Assertions.assertFalse(captor.getValue().isSuccess());
  }

  // Test scenario: deregister returns early without posting any event when serviceId is empty
  @Test
  public void deregisterReturnsEarlyWhenServiceIdIsEmpty() throws Exception {
    Mockito.when(registration.getServiceId()).thenReturn("");

    serviceRegistry.deregister(registration);

    Mockito.verify(namingService, Mockito.never()).deregisterInstance(
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyInt(), Mockito.anyString());
    Mockito.verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  // Test scenario: deregister does not post an event when a manager fails to deregister
  @Test
  public void deregisterDoesNotPostEventWhenPartialFailure() throws Exception {
    Mockito.doThrow(new NacosException(500, "deregister failed")).when(namingService)
        .deregisterInstance(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
            Mockito.anyInt(), Mockito.anyString());

    serviceRegistry.deregister(registration);

    Mockito.verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  // Test scenario: close shuts down all naming service managers without error
  @Test
  public void closeShutsDownAllManagers() throws Exception {
    serviceRegistry.close();

    Mockito.verify(namingServiceManager).shutDown();
  }

  // Test scenario: close continues shutting down remaining managers when one manager throws NacosException
  @Test
  public void closeContinuesWhenShutDownThrowsNacosException() throws Exception {
    Mockito.doThrow(new NacosException(500, "shutdown failed")).when(namingServiceManager).shutDown();

    Assertions.assertDoesNotThrow(() -> serviceRegistry.close());

    Mockito.verify(namingServiceManager).shutDown();
  }

  // Test scenario: setStatus with UP enables the instance and calls updateInstance on all managers
  @Test
  public void setStatusUpEnablesInstanceAndUpdates() throws Exception {
    serviceRegistry.setStatus(registration, NacosConst.STATUS_UP);

    Assertions.assertTrue(testInstance.isEnabled());
    Mockito.verify(namingMaintainService)
        .updateInstance("test-service", "DEFAULT_GROUP", testInstance);
  }

  // Test scenario: setStatus with DOWN disables the instance and calls updateInstance on all managers
  @Test
  public void setStatusDownDisablesInstanceAndUpdates() throws Exception {
    serviceRegistry.setStatus(registration, NacosConst.STATUS_DOWN);

    Assertions.assertFalse(testInstance.isEnabled());
    Mockito.verify(namingMaintainService)
        .updateInstance("test-service", "DEFAULT_GROUP", testInstance);
  }

  // Test scenario: setStatus ignores unsupported status values and performs no update
  @Test
  public void setStatusIgnoresInvalidStatus() throws Exception {
    serviceRegistry.setStatus(registration, "UNKNOWN");

    Mockito.verify(namingMaintainService, Mockito.never())
        .updateInstance(Mockito.anyString(), Mockito.anyString(), Mockito.any(Instance.class));
    Mockito.verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  // Test scenario: setStatus throws RuntimeException when updateInstance fails on the last manager
  @Test
  public void setStatusThrowsRuntimeExceptionWhenUpdateFailsOnLastManager() throws Exception {
    Mockito.doThrow(new RuntimeException("update failed")).when(namingMaintainService)
        .updateInstance(Mockito.anyString(), Mockito.anyString(), Mockito.any(Instance.class));

    RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
        () -> serviceRegistry.setStatus(registration, NacosConst.STATUS_UP));
    Assertions.assertEquals("update nacos instance status fail", ex.getMessage());
  }

  // Test scenario: setStatus continues to the next manager when updateInstance fails on a non-last manager
  @Test
  public void setStatusContinuesWhenUpdateFailsOnNonLastManager() throws Exception {
    NamingServiceManager firstManager = Mockito.mock(NamingServiceManager.class);
    NamingMaintainService firstMaintainService = Mockito.mock(NamingMaintainService.class);
    Mockito.when(firstManager.getNamingMaintainService()).thenReturn(firstMaintainService);
    Mockito.when(firstManager.getServerAddr()).thenReturn("first:8848");
    Mockito.when(firstManager.getOrder()).thenReturn(0);
    Mockito.doThrow(new RuntimeException("first failed")).when(firstMaintainService)
        .updateInstance(Mockito.anyString(), Mockito.anyString(), Mockito.any(Instance.class));

    NacosServiceRegistry registry = new NacosServiceRegistry(
        List.of(firstManager, namingServiceManager), nacosDiscoveryProperties, environment);

    Assertions.assertDoesNotThrow(() -> registry.setStatus(registration, NacosConst.STATUS_UP));
    Mockito.verify(firstMaintainService)
        .updateInstance("test-service", "DEFAULT_GROUP", testInstance);
    Mockito.verify(namingMaintainService)
        .updateInstance("test-service", "DEFAULT_GROUP", testInstance);
  }

  // Test scenario: getStatus returns UP when a matching enabled instance is found
  @Test
  public void getStatusReturnsUpWhenInstanceEnabled() throws Exception {
    Instance found = new Instance();
    found.setIp("127.0.0.1");
    found.setPort(8080);
    found.setEnabled(true);
    Mockito.when(namingService.getAllInstances("test-service", "DEFAULT_GROUP"))
        .thenReturn(List.of(found));

    String result = serviceRegistry.getStatus(registration);

    Assertions.assertEquals(NacosConst.STATUS_UP, result);
  }

  // Test scenario: getStatus returns DOWN when a matching disabled instance is found
  @Test
  public void getStatusReturnsDownWhenInstanceDisabled() throws Exception {
    Instance found = new Instance();
    found.setIp("127.0.0.1");
    found.setPort(8080);
    found.setEnabled(false);
    Mockito.when(namingService.getAllInstances("test-service", "DEFAULT_GROUP"))
        .thenReturn(List.of(found));

    String result = serviceRegistry.getStatus(registration);

    Assertions.assertEquals(NacosConst.STATUS_DOWN, result);
  }

  // Test scenario: getStatus returns null when no instance matches the configured ip and port
  @Test
  public void getStatusReturnsNullWhenNoMatchingInstance() throws Exception {
    Instance other = new Instance();
    other.setIp("10.0.0.1");
    other.setPort(9999);
    Mockito.when(namingService.getAllInstances("test-service", "DEFAULT_GROUP"))
        .thenReturn(List.of(other));

    String result = serviceRegistry.getStatus(registration);

    Assertions.assertNull(result);
  }

  // Test scenario: getStatus returns null when getAllInstances throws an exception
  @Test
  public void getStatusReturnsNullWhenGetAllInstancesThrows() throws Exception {
    Mockito.doThrow(new NacosException(500, "query failed")).when(namingService)
        .getAllInstances(Mockito.anyString(), Mockito.anyString());

    String result = serviceRegistry.getStatus(registration);

    Assertions.assertNull(result);
  }

  // Test scenario: getStatus returns UP from the second manager when the first manager throws
  @Test
  public void getStatusReturnsUpFromSecondManagerWhenFirstThrows() throws Exception {
    NamingServiceManager firstManager = Mockito.mock(NamingServiceManager.class);
    NamingService firstNamingService = Mockito.mock(NamingService.class);
    Mockito.when(firstManager.getNamingService()).thenReturn(firstNamingService);
    Mockito.when(firstManager.getServerAddr()).thenReturn("first:8848");
    Mockito.when(firstManager.getOrder()).thenReturn(0);
    Mockito.doThrow(new NacosException(500, "first failed")).when(firstNamingService)
        .getAllInstances(Mockito.anyString(), Mockito.anyString());

    Instance found = new Instance();
    found.setIp("127.0.0.1");
    found.setPort(8080);
    found.setEnabled(true);
    Mockito.when(namingService.getAllInstances("test-service", "DEFAULT_GROUP"))
        .thenReturn(List.of(found));

    NacosServiceRegistry registry = new NacosServiceRegistry(
        List.of(firstManager, namingServiceManager), nacosDiscoveryProperties, environment);

    String result = registry.getStatus(registration);

    Assertions.assertEquals(NacosConst.STATUS_UP, result);
  }
}