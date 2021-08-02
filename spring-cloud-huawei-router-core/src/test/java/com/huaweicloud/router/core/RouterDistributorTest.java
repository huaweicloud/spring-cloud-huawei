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

package com.huaweicloud.router.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.junit.Assert;
import org.junit.Test;

import com.huaweicloud.router.core.cache.RouterRuleCache;
import com.huaweicloud.router.core.distribute.AbstractRouterDistributor;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServer;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServiceInstance;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import mockit.Expectations;

/**
 * @Author GuoYl123
 * @Date 2019/11/4
 **/
public class RouterDistributorTest {

  private static String ruleStr = ""
      + "      - precedence: 2 #优先级\n"
      + "        match:        #匹配策略\n"
      + "          source: xx #匹配某个服务名\n"
      + "          headers:          #header匹配\n"
      + "            xx:\n"
      + "              regex: xx\n"
      + "              caseInsensitive: false # 是否区分大小写，默认为false，区分大小写\n"
      + "            xxx:\n"
      + "              exact: xx\n"
      + "        route: #路由规则\n"
      + "          - weight: 50\n"
      + "            tags:\n"
      + "              version: 1.1\n"
      + "      - precedence: 1\n"
      + "        match:\n"
      + "          source: 1 #匹配某个服务名\n"
      + "          headers:          #header匹配\n"
      + "            xx:\n"
      + "              regex: xx\n"
      + "              caseInsensitive: false # 是否区分大小写，默认为false，区分大小写\n"
      + "            xxx:\n"
      + "              exact: xxx\n"
      + "        route:\n"
      + "          - weight: 1\n"
      + "            tags:\n"
      + "              version: 1\n"
      + "              app: a";

  String targetServiceName = "test_server";

  @Test
  public void testVersionNotMatch() {
    Map<String, String> headerMap = new HashMap();
    headerMap.put("xxx", "xx");
    headerMap.put("xx", "xx");
    headerMap.put("formate", "json");
    List<ServiceCombServer> list = getMockList();
    list.remove(1);
    List<ServiceCombServer> serverList = mainFilter(list, headerMap);
    serverList.get(0).getHost().equals("01");
    Assert.assertEquals(1, serverList.size());
    Assert.assertEquals("service-01", serverList.get(0).getHost());
  }

  @Test
  public void testLowCase() {
    Map<String, String> headerMap = new HashMap();
    headerMap.put("xxx", "xx");
    headerMap.put("xx", "xx");
    headerMap.put("FoRmaTe", "json");
    List<ServiceCombServer> serverList = mainFilter(getMockList(), headerMap);
    Assert.assertEquals(1, serverList.size());
    Assert.assertEquals("service-02", serverList.get(0).getHost());
  }


  @Test
  public void testVersionMatch() {
    Map<String, String> headerMap = new HashMap();
    headerMap.put("xxx", "xx");
    headerMap.put("xx", "xx");
    headerMap.put("formate", "json");
    List<ServiceCombServer> serverList = mainFilter(getMockList(), headerMap);
    Assert.assertEquals(1, serverList.size());
    Assert.assertEquals("service-02", serverList.get(0).getHost());
  }

  private List<ServiceCombServer> getMockList() {
    List<ServiceCombServer> serverList = new ArrayList<>();

    MicroserviceInstance microserviceInstance1 = new MicroserviceInstance();
    Microservice microservice1 = new Microservice();
    microservice1.setServiceName(targetServiceName);
    microserviceInstance1.setMicroservice(microservice1);
    microserviceInstance1.setServiceId(microservice1.getServiceId());
    microserviceInstance1.setInstanceId("01");
    microserviceInstance1.setVersion("2.0");
    List<String> endpoints1 = new ArrayList<>();
    endpoints1.add("rest://service-01:8080");
    microserviceInstance1.setEndpoints(endpoints1);
    microserviceInstance1.setStatus(MicroserviceInstanceStatus.UP);
    ServiceCombServiceInstance serviceCombServiceInstance1 = new ServiceCombServiceInstance(microserviceInstance1);
    ServiceCombServer ins1 = new ServiceCombServer(serviceCombServiceInstance1);

    MicroserviceInstance microserviceInstance2 = new MicroserviceInstance();
    Microservice microservice2 = new Microservice();
    microservice2.setServiceName(targetServiceName);
    microserviceInstance2.setMicroservice(microservice2);
    microserviceInstance2.setServiceId(microservice2.getServiceId());
    microserviceInstance2.setInstanceId("02");
    microserviceInstance2.setVersion("1.1");
    List<String> endpoints2 = new ArrayList<>();
    endpoints2.add("rest://service-02:8080");
    microserviceInstance2.setEndpoints(endpoints2);
    microserviceInstance2.setStatus(MicroserviceInstanceStatus.UP);
    microserviceInstance2.getProperties().put("app", "a");
    ServiceCombServiceInstance serviceCombServiceInstance2 = new ServiceCombServiceInstance(microserviceInstance2);
    ServiceCombServer ins2 = new ServiceCombServer(serviceCombServiceInstance2);

    serverList.add(ins1);
    serverList.add(ins2);
    return serverList;
  }

  private List<ServiceCombServer> mainFilter(List<ServiceCombServer> serverList, Map<String, String> headerMap) {
    TestDistributor TestDistributor = new TestDistributor();
    DynamicPropertyFactory dpf = DynamicPropertyFactory.getInstance();
    DynamicStringProperty stringProperty = new DynamicStringProperty("", ruleStr);
    new Expectations(dpf) {
      {
        dpf.getStringProperty(anyString, null, (Runnable) any);
        result = stringProperty;
      }
    };
    RouterRuleCache.refresh();
    return RouterFilter
        .getFilteredListOfServers(serverList, targetServiceName, headerMap,
            TestDistributor);
  }

  class TestDistributor extends AbstractRouterDistributor<ServiceCombServer> {

    public TestDistributor() {
      init();
    }
  }
}
