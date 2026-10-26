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

package com.huaweicloud.nacos.discovery.logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;

public class MasterStandbyNacosLogbackServiceTest {

  private LoggerContext loggerContext;

  @BeforeEach
  public void setUp() {
    loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
  }

  @AfterEach
  public void tearDown() {
    // remove listeners added during tests to keep test isolation
    loggerContext.getCopyOfListenerList().stream()
        .filter(l -> l instanceof MasterStandbyNacosLogbackService.MasterStandbyNacosLoggerContextListener)
        .forEach(loggerContext::removeListener);
  }

  // Test scenario: getInstance returns a non-null singleton and the same instance across calls
  @Test
  public void getInstanceReturnsSingleton() {
    MasterStandbyNacosLogbackService instance1 = MasterStandbyNacosLogbackService.getInstance();
    MasterStandbyNacosLogbackService instance2 = MasterStandbyNacosLogbackService.getInstance();

    Assertions.assertNotNull(instance1);
    Assertions.assertSame(instance1, instance2);
  }

  // Test scenario: loadConfiguration loads logback config and adds a MasterStandbyNacosLoggerContextListener
  @Test
  public void loadConfigurationAddsListenerToLoggerContext() {
    MasterStandbyNacosLogbackService service = MasterStandbyNacosLogbackService.getInstance();
    int listenerCountBefore = loggerContext.getCopyOfListenerList().size();

    service.loadConfiguration();

    List<LoggerContextListener> listeners = loggerContext.getCopyOfListenerList();
    Assertions.assertEquals(listenerCountBefore + 1, listeners.size());
    Assertions.assertTrue(listeners.stream()
        .anyMatch(l -> l instanceof MasterStandbyNacosLogbackService.MasterStandbyNacosLoggerContextListener));
  }

  // Test scenario: getResourceUrl returns a valid URL pointing to master-standby-logback.xml
  @Test
  public void getResourceUrlReturnsValidUrl() throws Exception {
    MasterStandbyNacosLogbackService service = MasterStandbyNacosLogbackService.getInstance();
    Method method = MasterStandbyNacosLogbackService.class.getDeclaredMethod("getResourceUrl");
    method.setAccessible(true);

    URL url = (URL) method.invoke(service);

    Assertions.assertNotNull(url);
    Assertions.assertTrue(url.toExternalForm().contains("master-standby-logback.xml"));
  }

  // Test scenario: loadConfigurationOnStart returns the LoggerContext when config loads successfully
  @Test
  public void loadConfigurationOnStartReturnsLoggerContext() throws Exception {
    MasterStandbyNacosLogbackService service = MasterStandbyNacosLogbackService.getInstance();
    Method method = MasterStandbyNacosLogbackService.class.getDeclaredMethod("loadConfigurationOnStart");
    method.setAccessible(true);

    Object result = method.invoke(service);

    Assertions.assertNotNull(result);
    Assertions.assertSame(loggerContext, result);
  }

  // Test scenario: listener isResetResistant returns true
  @Test
  public void listenerIsResetResistantReturnsTrue() {
    MasterStandbyNacosLogbackService service = MasterStandbyNacosLogbackService.getInstance();
    service.loadConfiguration();

    MasterStandbyNacosLogbackService.MasterStandbyNacosLoggerContextListener listener = getLastListener();
    Assertions.assertNotNull(listener);
    Assertions.assertTrue(listener.isResetResistant());
  }

  // Test scenario: listener onReset reloads configuration via loadConfigurationOnStart without throwing
  @Test
  public void listenerOnResetReloadsConfiguration() {
    MasterStandbyNacosLogbackService service = MasterStandbyNacosLogbackService.getInstance();
    service.loadConfiguration();

    MasterStandbyNacosLogbackService.MasterStandbyNacosLoggerContextListener listener = getLastListener();
    Assertions.assertNotNull(listener);

    Assertions.assertDoesNotThrow(() -> listener.onReset(loggerContext));
  }

  // Test scenario: listener onStart does not throw and performs no operation
  @Test
  public void listenerOnStartDoesNotThrow() {
    MasterStandbyNacosLogbackService service = MasterStandbyNacosLogbackService.getInstance();
    service.loadConfiguration();

    MasterStandbyNacosLogbackService.MasterStandbyNacosLoggerContextListener listener = getLastListener();
    Assertions.assertNotNull(listener);

    Assertions.assertDoesNotThrow(() -> listener.onStart(loggerContext));
  }

  // Test scenario: listener onStop does not throw and performs no operation
  @Test
  public void listenerOnStopDoesNotThrow() {
    MasterStandbyNacosLogbackService service = MasterStandbyNacosLogbackService.getInstance();
    service.loadConfiguration();

    MasterStandbyNacosLogbackService.MasterStandbyNacosLoggerContextListener listener = getLastListener();
    Assertions.assertNotNull(listener);

    Assertions.assertDoesNotThrow(() -> listener.onStop(loggerContext));
  }

  // Test scenario: listener onLevelChange does not throw and performs no operation
  @Test
  public void listenerOnLevelChangeDoesNotThrow() {
    MasterStandbyNacosLogbackService service = MasterStandbyNacosLogbackService.getInstance();
    service.loadConfiguration();

    MasterStandbyNacosLogbackService.MasterStandbyNacosLoggerContextListener listener = getLastListener();
    Assertions.assertNotNull(listener);

    Logger logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
    Assertions.assertDoesNotThrow(() -> listener.onLevelChange(logbackLogger, Level.DEBUG));
  }

  private MasterStandbyNacosLogbackService.MasterStandbyNacosLoggerContextListener getLastListener() {
    List<LoggerContextListener> listeners = loggerContext.getCopyOfListenerList();
    return listeners.stream()
        .filter(l -> l instanceof MasterStandbyNacosLogbackService.MasterStandbyNacosLoggerContextListener)
        .map(l -> (MasterStandbyNacosLogbackService.MasterStandbyNacosLoggerContextListener) l)
        .reduce((first, second) -> second)
        .orElse(null);
  }
}