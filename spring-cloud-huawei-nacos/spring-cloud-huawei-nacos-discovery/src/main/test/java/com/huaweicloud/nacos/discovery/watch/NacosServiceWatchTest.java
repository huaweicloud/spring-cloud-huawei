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

package com.huaweicloud.nacos.discovery.watch;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.huaweicloud.common.event.ClosedEventListener;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;
import com.huaweicloud.nacos.discovery.manager.NamingServiceManager;
import com.huaweicloud.nacos.discovery.registry.NacosServiceRegistrationEvent;

public class NacosServiceWatchTest {

  private NacosDiscoveryProperties properties;

  private NamingServiceManager managerLow;

  private NamingServiceManager managerHigh;

  private NamingService namingServiceLow;

  private NamingService namingServiceHigh;

  private ClosedEventListener closedEventListener;

  private NacosServiceWatch watch;

  @BeforeEach
  public void setUp() {
    properties = new NacosDiscoveryProperties();
    properties.setService("test-service");
    properties.setGroup("test-group");
    properties.setClusterName("test-cluster");
    properties.setIp("127.0.0.1");
    properties.setPort(8080);
    Map<String, String> metadata = new HashMap<>();
    metadata.put("version", "1.0");
    properties.setMetadata(metadata);

    namingServiceLow = Mockito.mock(NamingService.class);
    namingServiceHigh = Mockito.mock(NamingService.class);

    managerLow = Mockito.mock(NamingServiceManager.class);
    managerHigh = Mockito.mock(NamingServiceManager.class);

    Mockito.when(managerLow.getOrder()).thenReturn(1);
    Mockito.when(managerHigh.getOrder()).thenReturn(200);
    Mockito.when(managerLow.getNamingService()).thenReturn(namingServiceLow);
    Mockito.when(managerHigh.getNamingService()).thenReturn(namingServiceHigh);
    Mockito.when(managerLow.getServerAddr()).thenReturn("127.0.0.1:8848");
    Mockito.when(managerHigh.getServerAddr()).thenReturn("127.0.0.2:8848");

    closedEventListener = new ClosedEventListener();

    watch = new NacosServiceWatch(properties, Arrays.asList(managerHigh, managerLow), closedEventListener);
  }

  // Test scenario: constructor sorts managers by order, subscribe called in ascending order
  @Test
  public void constructorSortsManagersByOrder() throws NacosException {
    Instance instance = new Instance();
    instance.setIp("127.0.0.1");
    instance.setPort(8080);
    NacosServiceRegistrationEvent event = new NacosServiceRegistrationEvent(instance, true);

    watch.subscribeServiceMetadata(event);

    InOrder inOrder = Mockito.inOrder(namingServiceLow, namingServiceHigh);
    inOrder.verify(namingServiceLow).subscribe(
        Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), Mockito.any(EventListener.class));
    inOrder.verify(namingServiceHigh).subscribe(
        Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), Mockito.any(EventListener.class));
  }

  // Test scenario: subscribeServiceMetadata with success subscribes to all managers with correct parameters
  @Test
  public void subscribeServiceMetadataSuccessSubscribesToAllManagers() throws NacosException {
    Instance instance = new Instance();
    instance.setIp("127.0.0.1");
    instance.setPort(8080);
    NacosServiceRegistrationEvent event = new NacosServiceRegistrationEvent(instance, true);

    watch.subscribeServiceMetadata(event);

    Mockito.verify(namingServiceLow).subscribe(Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), Mockito.any(EventListener.class));
    Mockito.verify(namingServiceHigh).subscribe(Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), Mockito.any(EventListener.class));
  }

  // Test scenario: subscribeServiceMetadata with failure does not subscribe to any manager
  @Test
  public void subscribeServiceMetadataFailureDoesNotSubscribe() throws NacosException {
    Instance instance = new Instance();
    NacosServiceRegistrationEvent event = new NacosServiceRegistrationEvent(instance, false);

    watch.subscribeServiceMetadata(event);

    Mockito.verify(namingServiceLow, Mockito.never()).subscribe(
        Mockito.anyString(), Mockito.anyString(), Mockito.anyList(), Mockito.any(EventListener.class));
    Mockito.verify(namingServiceHigh, Mockito.never()).subscribe(
        Mockito.anyString(), Mockito.anyString(), Mockito.anyList(), Mockito.any(EventListener.class));
  }

  // Test scenario: subscribeServiceMetadata continues to next manager when subscribe throws exception
  @Test
  public void subscribeServiceMetadataContinuesWhenSubscribeThrows() throws NacosException {
    Mockito.doThrow(new NacosException(NacosException.SERVER_ERROR, "subscribe error"))
        .when(namingServiceLow).subscribe(Mockito.anyString(), Mockito.anyString(),
            Mockito.anyList(), Mockito.any(EventListener.class));

    Instance instance = new Instance();
    instance.setIp("127.0.0.1");
    instance.setPort(8080);
    NacosServiceRegistrationEvent event = new NacosServiceRegistrationEvent(instance, true);

    watch.subscribeServiceMetadata(event);

    Mockito.verify(namingServiceHigh).subscribe(Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), Mockito.any(EventListener.class));
  }

  // Test scenario: event listener resets metadata when matching instance has different metadata
  @Test
  public void eventListenerResetsMetadataWhenChanged() throws NacosException {
    Instance regInstance = new Instance();
    regInstance.setIp("127.0.0.1");
    regInstance.setPort(8080);
    NacosServiceRegistrationEvent event = new NacosServiceRegistrationEvent(regInstance, true);

    watch.subscribeServiceMetadata(event);

    ArgumentCaptor<EventListener> captor = ArgumentCaptor.forClass(EventListener.class);
    Mockito.verify(namingServiceLow).subscribe(Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), captor.capture());
    EventListener listener = captor.getValue();

    Instance changedInstance = new Instance();
    changedInstance.setIp("127.0.0.1");
    changedInstance.setPort(8080);
    Map<String, String> newMetadata = new HashMap<>();
    newMetadata.put("version", "2.0");
    newMetadata.put("region", "us-east");
    changedInstance.setMetadata(newMetadata);

    NamingEvent namingEvent = new NamingEvent("test-service", Collections.singletonList(changedInstance));

    Map<String, String> originalMetadata = new HashMap<>(properties.getMetadata());

    listener.onEvent(namingEvent);

    Assertions.assertEquals(newMetadata, properties.getMetadata());
    Assertions.assertEquals("2.0", properties.getMetadata().get("version"));
    Assertions.assertEquals("us-east", properties.getMetadata().get("region"));
    Assertions.assertNotEquals(originalMetadata, properties.getMetadata());
  }

  // Test scenario: event listener does not reset metadata when matching instance has same metadata
  @Test
  public void eventListenerDoesNotResetWhenMetadataSame() throws NacosException {
    Instance regInstance = new Instance();
    regInstance.setIp("127.0.0.1");
    regInstance.setPort(8080);
    NacosServiceRegistrationEvent event = new NacosServiceRegistrationEvent(regInstance, true);

    watch.subscribeServiceMetadata(event);

    ArgumentCaptor<EventListener> captor = ArgumentCaptor.forClass(EventListener.class);
    Mockito.verify(namingServiceLow).subscribe(Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), captor.capture());
    EventListener listener = captor.getValue();

    Instance sameInstance = new Instance();
    sameInstance.setIp("127.0.0.1");
    sameInstance.setPort(8080);
    Map<String, String> sameMetadata = new HashMap<>();
    sameMetadata.put("version", "1.0");
    sameInstance.setMetadata(sameMetadata);

    NamingEvent namingEvent = new NamingEvent("test-service", Collections.singletonList(sameInstance));

    Map<String, String> metadataBefore = properties.getMetadata();

    listener.onEvent(namingEvent);

    Assertions.assertSame(metadataBefore, properties.getMetadata());
    Assertions.assertEquals("1.0", properties.getMetadata().get("version"));
  }

  // Test scenario: event listener does not reset metadata when no matching instance found
  @Test
  public void eventListenerDoesNotResetWhenNoMatchingInstance() throws NacosException {
    Instance regInstance = new Instance();
    regInstance.setIp("127.0.0.1");
    regInstance.setPort(8080);
    NacosServiceRegistrationEvent event = new NacosServiceRegistrationEvent(regInstance, true);

    watch.subscribeServiceMetadata(event);

    ArgumentCaptor<EventListener> captor = ArgumentCaptor.forClass(EventListener.class);
    Mockito.verify(namingServiceLow).subscribe(Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), captor.capture());
    EventListener listener = captor.getValue();

    Instance otherInstance = new Instance();
    otherInstance.setIp("10.0.0.99");
    otherInstance.setPort(9999);
    Map<String, String> otherMetadata = new HashMap<>();
    otherMetadata.put("version", "3.0");
    otherInstance.setMetadata(otherMetadata);

    NamingEvent namingEvent = new NamingEvent("test-service", Collections.singletonList(otherInstance));

    Map<String, String> metadataBefore = properties.getMetadata();

    listener.onEvent(namingEvent);

    Assertions.assertSame(metadataBefore, properties.getMetadata());
    Assertions.assertEquals("1.0", properties.getMetadata().get("version"));
  }

  // Test scenario: event listener handles NamingEvent with empty instance list without errors
  @Test
  public void eventListenerHandlesEmptyInstanceList() throws NacosException {
    Instance regInstance = new Instance();
    regInstance.setIp("127.0.0.1");
    regInstance.setPort(8080);
    NacosServiceRegistrationEvent event = new NacosServiceRegistrationEvent(regInstance, true);

    watch.subscribeServiceMetadata(event);

    ArgumentCaptor<EventListener> captor = ArgumentCaptor.forClass(EventListener.class);
    Mockito.verify(namingServiceLow).subscribe(Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), captor.capture());
    EventListener listener = captor.getValue();

    NamingEvent namingEvent = new NamingEvent("test-service", Collections.emptyList());

    Map<String, String> metadataBefore = properties.getMetadata();

    listener.onEvent(namingEvent);

    Assertions.assertSame(metadataBefore, properties.getMetadata());
    Assertions.assertEquals("1.0", properties.getMetadata().get("version"));
  }

  // Test scenario: event listener selects correct instance from a mixed list (one matching, one not)
  @Test
  public void eventListenerSelectsMatchingInstanceFromMixedList() throws NacosException {
    Instance regInstance = new Instance();
    regInstance.setIp("127.0.0.1");
    regInstance.setPort(8080);
    NacosServiceRegistrationEvent event = new NacosServiceRegistrationEvent(regInstance, true);

    watch.subscribeServiceMetadata(event);

    ArgumentCaptor<EventListener> captor = ArgumentCaptor.forClass(EventListener.class);
    Mockito.verify(namingServiceLow).subscribe(Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), captor.capture());
    EventListener listener = captor.getValue();

    Instance nonMatching = new Instance();
    nonMatching.setIp("10.0.0.50");
    nonMatching.setPort(7070);
    nonMatching.setMetadata(new HashMap<>());

    Instance matching = new Instance();
    matching.setIp("127.0.0.1");
    matching.setPort(8080);
    Map<String, String> newMetadata = new HashMap<>();
    newMetadata.put("version", "5.0");
    matching.setMetadata(newMetadata);

    NamingEvent namingEvent = new NamingEvent("test-service", Arrays.asList(nonMatching, matching));

    listener.onEvent(namingEvent);

    Assertions.assertEquals(newMetadata, properties.getMetadata());
    Assertions.assertEquals("5.0", properties.getMetadata().get("version"));
  }

  // Test scenario: event listener ignores non-NamingEvent events
  @Test
  public void eventListenerIgnoresNonNamingEvent() throws NacosException {
    Instance regInstance = new Instance();
    regInstance.setIp("127.0.0.1");
    regInstance.setPort(8080);
    NacosServiceRegistrationEvent event = new NacosServiceRegistrationEvent(regInstance, true);

    watch.subscribeServiceMetadata(event);

    ArgumentCaptor<EventListener> captor = ArgumentCaptor.forClass(EventListener.class);
    Mockito.verify(namingServiceLow).subscribe(Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), captor.capture());
    EventListener listener = captor.getValue();

    Map<String, String> metadataBefore = properties.getMetadata();

    Event nonNamingEvent = new Event() {
    };

    listener.onEvent(nonNamingEvent);

    Assertions.assertSame(metadataBefore, properties.getMetadata());
    Assertions.assertEquals("1.0", properties.getMetadata().get("version"));
  }

  // Test scenario: stopSubscribe unsubscribes from all managers with the same eventListener when context closed
  @Test
  public void stopSubscribeUnsubscribesFromAllManagers() throws NacosException {
    Instance regInstance = new Instance();
    regInstance.setIp("127.0.0.1");
    regInstance.setPort(8080);
    NacosServiceRegistrationEvent regEvent = new NacosServiceRegistrationEvent(regInstance, true);
    watch.subscribeServiceMetadata(regEvent);

    ArgumentCaptor<EventListener> captor = ArgumentCaptor.forClass(EventListener.class);
    Mockito.verify(namingServiceLow).subscribe(Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), captor.capture());
    EventListener capturedListener = captor.getValue();

    ApplicationContext mockContext = Mockito.mock(ApplicationContext.class);
    ContextClosedEvent closedEvent = new ContextClosedEvent(mockContext);
    closedEventListener.onApplicationEvent(closedEvent);

    Mockito.verify(namingServiceLow).unsubscribe("test-service", "test-group",
        Collections.singletonList("test-cluster"), capturedListener);
    Mockito.verify(namingServiceHigh).unsubscribe("test-service", "test-group",
        Collections.singletonList("test-cluster"), capturedListener);
  }

  // Test scenario: stopSubscribe continues to next manager when unsubscribe throws exception
  @Test
  public void stopSubscribeContinuesWhenUnsubscribeThrows() throws NacosException {
    Instance regInstance = new Instance();
    regInstance.setIp("127.0.0.1");
    regInstance.setPort(8080);
    NacosServiceRegistrationEvent regEvent = new NacosServiceRegistrationEvent(regInstance, true);
    watch.subscribeServiceMetadata(regEvent);

    Mockito.doThrow(new NacosException(NacosException.SERVER_ERROR, "unsubscribe error"))
        .when(namingServiceLow).unsubscribe(Mockito.anyString(), Mockito.anyString(),
            Mockito.anyList(), Mockito.any(EventListener.class));

    ApplicationContext mockContext = Mockito.mock(ApplicationContext.class);
    ContextClosedEvent closedEvent = new ContextClosedEvent(mockContext);
    closedEventListener.onApplicationEvent(closedEvent);

    Mockito.verify(namingServiceLow).unsubscribe(Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), Mockito.any(EventListener.class));
    Mockito.verify(namingServiceHigh).unsubscribe(Mockito.eq("test-service"), Mockito.eq("test-group"),
        Mockito.eq(Collections.singletonList("test-cluster")), Mockito.any(EventListener.class));
  }
}