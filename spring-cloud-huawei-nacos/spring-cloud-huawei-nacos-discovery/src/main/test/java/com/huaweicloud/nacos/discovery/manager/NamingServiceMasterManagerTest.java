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

package com.huaweicloud.nacos.discovery.manager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.NacosNamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.huaweicloud.nacos.discovery.NacosConst;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;

public class NamingServiceMasterManagerTest {

  private NacosDiscoveryProperties properties;

  private ThreadPoolTaskScheduler mockScheduler;

  @BeforeEach
  public void setUp() {
    properties = new NacosDiscoveryProperties();
    properties.setServerAddr("127.0.0.1:8848");
    properties.setOrder(200);
    properties.setMasterStandbyEnabled(false);
    properties.setNamingServiceCheckTaskDelay(5000);

    mockScheduler = Mockito.mock(ThreadPoolTaskScheduler.class);
  }

  /**
   * Creates a NamingServiceMasterManager with NamingServiceManagerUtils.buildTaskScheduler mocked
   * to return the shared mockScheduler. The MockedStatic must remain open for the lifetime of
   * the returned manager whenever methods that call NamingServiceManagerUtils are invoked.
   */
  private NamingServiceMasterManager createManager(MockedStatic<NamingServiceManagerUtils> ms) {
    ms.when(() -> NamingServiceManagerUtils.buildTaskScheduler(Mockito.anyString()))
        .thenReturn(mockScheduler);
    return new NamingServiceMasterManager(properties);
  }

  // Test scenario: constructor with masterStandbyEnabled=false does not start scheduler task and health is false
  @Test
  public void constructorMasterStandbyDisabledDoesNotStartScheduler() {
    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      NamingServiceMasterManager manager = createManager(ms);

      Mockito.verify(mockScheduler, Mockito.never())
          .scheduleWithFixedDelay(Mockito.any(Runnable.class), Mockito.any(Duration.class));
      Assertions.assertFalse(manager.isNacosServerHealth());
    }
  }

  // Test scenario: constructor with masterStandbyEnabled=true starts health check scheduler with correct delay
  @Test
  public void constructorMasterStandbyEnabledStartsSchedulerTask() {
    properties.setMasterStandbyEnabled(true);

    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      NamingServiceMasterManager manager = createManager(ms);

      Mockito.verify(mockScheduler).scheduleWithFixedDelay(
          Mockito.any(Runnable.class),
          Mockito.eq(Duration.ofMillis(5000)));
      Assertions.assertFalse(manager.isNacosServerHealth());
    }
  }

  // Test scenario: getServerAddr returns the server address configured in properties
  @Test
  public void getServerAddrReturnsPropertiesServerAddr() {
    properties.setServerAddr("192.168.1.100:8848");

    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      NamingServiceMasterManager manager = createManager(ms);

      Assertions.assertEquals("192.168.1.100:8848", manager.getServerAddr());
    }
  }

  // Test scenario: getOrder returns the order value configured in properties
  @Test
  public void getOrderReturnsPropertiesOrder() {
    properties.setOrder(300);

    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      NamingServiceMasterManager manager = createManager(ms);

      Assertions.assertEquals(300, manager.getOrder());
    }
  }

  // Test scenario: isNacosServerHealth returns false before any naming service is created
  @Test
  public void isNacosServerHealthReturnsFalseInitially() {
    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      NamingServiceMasterManager manager = createManager(ms);

      Assertions.assertFalse(manager.isNacosServerHealth());
    }
  }

  // Test scenario: getNamingService creates a new NacosNamingService when null and sets health=true when server status is UP
  @Test
  public void getNamingServiceCreatesWhenNullAndSetsHealthTrueWhenUp() {
    Properties mockProps = new Properties();
    mockProps.put("serverAddr", "127.0.0.1:8848");

    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class);
        MockedConstruction<NacosNamingService> mc = Mockito.mockConstruction(NacosNamingService.class,
            (mock, context) -> {
              Mockito.when(mock.getServerStatus()).thenReturn(NacosConst.STATUS_UP);
            })) {
      ms.when(() -> NamingServiceManagerUtils.buildTaskScheduler(Mockito.anyString()))
          .thenReturn(mockScheduler);
      ms.when(() -> NamingServiceManagerUtils.buildMasterServerProperties(Mockito.any()))
          .thenReturn(mockProps);

      NamingServiceMasterManager manager = new NamingServiceMasterManager(properties);
      NamingService result = manager.getNamingService();

      Assertions.assertNotNull(result);
      Assertions.assertEquals(1, mc.constructed().size());
      Assertions.assertTrue(manager.isNacosServerHealth());
    }
  }

  // Test scenario: getNamingService returns the same instance on second call (double-checked locking)
  @Test
  public void getNamingServiceReturnsSameInstanceOnSecondCall() {
    Properties mockProps = new Properties();
    mockProps.put("serverAddr", "127.0.0.1:8848");

    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class);
        MockedConstruction<NacosNamingService> mc = Mockito.mockConstruction(NacosNamingService.class,
            (mock, context) -> {
              Mockito.when(mock.getServerStatus()).thenReturn(NacosConst.STATUS_DOWN);
            })) {
      ms.when(() -> NamingServiceManagerUtils.buildTaskScheduler(Mockito.anyString()))
          .thenReturn(mockScheduler);
      ms.when(() -> NamingServiceManagerUtils.buildMasterServerProperties(Mockito.any()))
          .thenReturn(mockProps);

      NamingServiceMasterManager manager = new NamingServiceMasterManager(properties);
      NamingService first = manager.getNamingService();
      NamingService second = manager.getNamingService();

      Assertions.assertSame(first, second);
      Assertions.assertEquals(1, mc.constructed().size());
      Assertions.assertFalse(manager.isNacosServerHealth());
    }
  }

  // Test scenario: getNamingService throws IllegalStateException when properties build fails
  @Test
  public void getNamingServiceThrowsIllegalStateExceptionWhenPropertiesFail() {
    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      ms.when(() -> NamingServiceManagerUtils.buildTaskScheduler(Mockito.anyString()))
          .thenReturn(mockScheduler);
      ms.when(() -> NamingServiceManagerUtils.buildMasterServerProperties(Mockito.any()))
          .thenThrow(new RuntimeException("properties build failed"));

      NamingServiceMasterManager manager = new NamingServiceMasterManager(properties);

      IllegalStateException ex = Assertions.assertThrows(
          IllegalStateException.class, manager::getNamingService);
      Assertions.assertTrue(ex.getMessage().contains("build namingService failed"));
      Assertions.assertNotNull(ex.getCause());
    }
  }

  // Test scenario: getNamingMaintainService creates a new NacosNamingMaintainService when null
  @Test
  public void getNamingMaintainServiceCreatesWhenNull() {
    Properties mockProps = new Properties();
    mockProps.put("serverAddr", "127.0.0.1:8848");

    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class);
        MockedConstruction<NacosNamingMaintainService> mc = Mockito.mockConstruction(
            NacosNamingMaintainService.class)) {
      ms.when(() -> NamingServiceManagerUtils.buildTaskScheduler(Mockito.anyString()))
          .thenReturn(mockScheduler);
      ms.when(() -> NamingServiceManagerUtils.buildMasterServerProperties(Mockito.any()))
          .thenReturn(mockProps);

      NamingServiceMasterManager manager = new NamingServiceMasterManager(properties);
      NamingMaintainService result = manager.getNamingMaintainService();

      Assertions.assertNotNull(result);
      Assertions.assertEquals(1, mc.constructed().size());
    }
  }

  // Test scenario: getNamingMaintainService returns the same instance on second call (double-checked locking)
  @Test
  public void getNamingMaintainServiceReturnsSameInstanceOnSecondCall() {
    Properties mockProps = new Properties();
    mockProps.put("serverAddr", "127.0.0.1:8848");

    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class);
        MockedConstruction<NacosNamingMaintainService> mc = Mockito.mockConstruction(
            NacosNamingMaintainService.class)) {
      ms.when(() -> NamingServiceManagerUtils.buildTaskScheduler(Mockito.anyString()))
          .thenReturn(mockScheduler);
      ms.when(() -> NamingServiceManagerUtils.buildMasterServerProperties(Mockito.any()))
          .thenReturn(mockProps);

      NamingServiceMasterManager manager = new NamingServiceMasterManager(properties);
      NamingMaintainService first = manager.getNamingMaintainService();
      NamingMaintainService second = manager.getNamingMaintainService();

      Assertions.assertSame(first, second);
      Assertions.assertEquals(1, mc.constructed().size());
    }
  }

  // Test scenario: getNamingMaintainService throws IllegalStateException when properties build fails
  @Test
  public void getNamingMaintainServiceThrowsIllegalStateExceptionWhenPropertiesFail() {
    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      ms.when(() -> NamingServiceManagerUtils.buildTaskScheduler(Mockito.anyString()))
          .thenReturn(mockScheduler);
      ms.when(() -> NamingServiceManagerUtils.buildMasterServerProperties(Mockito.any()))
          .thenThrow(new RuntimeException("properties build failed"));

      NamingServiceMasterManager manager = new NamingServiceMasterManager(properties);

      IllegalStateException ex = Assertions.assertThrows(
          IllegalStateException.class, manager::getNamingMaintainService);
      Assertions.assertTrue(ex.getMessage().contains("build namingService failed"));
      Assertions.assertNotNull(ex.getCause());
    }
  }

  // Test scenario: shutDown shuts down both namingService and namingMaintainService when both are set
  @Test
  public void shutDownShutsDownBothServicesWhenNotNull() throws Exception {
    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      NamingServiceMasterManager manager = createManager(ms);

      NamingService mockNamingService = Mockito.mock(NamingService.class);
      NamingMaintainService mockMaintainService = Mockito.mock(NamingMaintainService.class);
      setField(manager, "namingService", mockNamingService);
      setField(manager, "namingMaintainService", mockMaintainService);

      manager.shutDown();

      Mockito.verify(mockNamingService).shutDown();
      Mockito.verify(mockMaintainService).shutDown();
      Assertions.assertNull(getField(manager, "namingService"));
      Assertions.assertNull(getField(manager, "namingMaintainService"));
    }
  }

  // Test scenario: shutDown does nothing when both services are null
  @Test
  public void shutDownDoesNothingWhenBothNull() throws Exception {
    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      NamingServiceMasterManager manager = createManager(ms);

      manager.shutDown();

      Assertions.assertNull(getField(manager, "namingService"));
      Assertions.assertNull(getField(manager, "namingMaintainService"));
    }
  }

  // Test scenario: shutDown shuts down only namingService when namingMaintainService is null
  @Test
  public void shutDownShutsDownOnlyNamingServiceWhenMaintainNull() throws Exception {
    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      NamingServiceMasterManager manager = createManager(ms);

      NamingService mockNamingService = Mockito.mock(NamingService.class);
      setField(manager, "namingService", mockNamingService);

      manager.shutDown();

      Mockito.verify(mockNamingService).shutDown();
      Assertions.assertNull(getField(manager, "namingService"));
      Assertions.assertNull(getField(manager, "namingMaintainService"));
    }
  }

  // Test scenario: shutDown shuts down only namingMaintainService when namingService is null
  @Test
  public void shutDownShutsDownOnlyMaintainServiceWhenNamingNull() throws Exception {
    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      NamingServiceMasterManager manager = createManager(ms);

      NamingMaintainService mockMaintainService = Mockito.mock(NamingMaintainService.class);
      setField(manager, "namingMaintainService", mockMaintainService);

      manager.shutDown();

      Mockito.verify(mockMaintainService).shutDown();
      Assertions.assertNull(getField(manager, "namingService"));
      Assertions.assertNull(getField(manager, "namingMaintainService"));
    }
  }

  // Test scenario: namingServiceHealthCheck sets health to true when namingService server status is UP
  @Test
  public void namingServiceHealthCheckSetsHealthTrueWhenServerUp() throws Exception {
    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      NamingServiceMasterManager manager = createManager(ms);

      NamingService mockNamingService = Mockito.mock(NamingService.class);
      Mockito.when(mockNamingService.getServerStatus()).thenReturn(NacosConst.STATUS_UP);
      setField(manager, "namingService", mockNamingService);
      setField(manager, "isServerHealth", false);

      invokePrivateMethod(manager, "namingServiceHealthCheck");

      Assertions.assertTrue(manager.isNacosServerHealth());
    }
  }

  // Test scenario: namingServiceHealthCheck sets health to false when namingService server status is DOWN
  @Test
  public void namingServiceHealthCheckSetsHealthFalseWhenServerDown() throws Exception {
    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      NamingServiceMasterManager manager = createManager(ms);

      NamingService mockNamingService = Mockito.mock(NamingService.class);
      Mockito.when(mockNamingService.getServerStatus()).thenReturn(NacosConst.STATUS_DOWN);
      setField(manager, "namingService", mockNamingService);
      setField(manager, "isServerHealth", true);

      invokePrivateMethod(manager, "namingServiceHealthCheck");

      Assertions.assertFalse(manager.isNacosServerHealth());
    }
  }

  // Test scenario: namingServiceHealthCheck does nothing when namingService is null, health remains unchanged
  @Test
  public void namingServiceHealthCheckDoesNothingWhenNamingServiceNull() throws Exception {
    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class)) {
      NamingServiceMasterManager manager = createManager(ms);
      setField(manager, "isServerHealth", true);

      invokePrivateMethod(manager, "namingServiceHealthCheck");

      Assertions.assertTrue(manager.isNacosServerHealth());
    }
  }

  // Test scenario: scheduled health check runnable updates health status when namingService is available
  @Test
  public void scheduledHealthCheckRunnableUpdatesHealthStatus() throws Exception {
    properties.setMasterStandbyEnabled(true);
    Properties mockProps = new Properties();
    mockProps.put("serverAddr", "127.0.0.1:8848");

    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

    try (MockedStatic<NamingServiceManagerUtils> ms = Mockito.mockStatic(NamingServiceManagerUtils.class);
        MockedConstruction<NacosNamingService> mc = Mockito.mockConstruction(NacosNamingService.class,
            (mock, context) -> {
              Mockito.when(mock.getServerStatus()).thenReturn(NacosConst.STATUS_UP);
            })) {
      ms.when(() -> NamingServiceManagerUtils.buildTaskScheduler(Mockito.anyString()))
          .thenReturn(mockScheduler);
      ms.when(() -> NamingServiceManagerUtils.buildMasterServerProperties(Mockito.any()))
          .thenReturn(mockProps);

      NamingServiceMasterManager manager = new NamingServiceMasterManager(properties);

      Mockito.verify(mockScheduler).scheduleWithFixedDelay(runnableCaptor.capture(),
          Mockito.any(Duration.class));

      Assertions.assertFalse(manager.isNacosServerHealth());

      manager.getNamingService();
      Assertions.assertTrue(manager.isNacosServerHealth());

      NacosNamingService constructedMock = mc.constructed().get(0);
      Mockito.when(constructedMock.getServerStatus()).thenReturn(NacosConst.STATUS_DOWN);

      runnableCaptor.getValue().run();

      Assertions.assertFalse(manager.isNacosServerHealth());
    }
  }

  // ---------- Reflection helpers ----------

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  private Object getField(Object target, String fieldName) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(target);
  }

  private void invokePrivateMethod(Object target, String methodName) throws Exception {
    Method method = target.getClass().getDeclaredMethod(methodName);
    method.setAccessible(true);
    method.invoke(target);
  }
}