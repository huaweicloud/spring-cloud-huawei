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

package org.springframework.cloud.canary.core;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.loadbalancer.Server;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.springframework.cloud.canary.core.distribute.AbstractCanaryDistributer;
import mockit.Expectations;

/**
 * @Author GuoYl123
 * @Date 2019/11/4
 **/
public class CanaryDistributerTest {

  String ruleStr = ""
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
      + "              version: 11\n"
      + "      - precedence: 1\n"
      + "        match:\n"
      + "          source: 1 #匹配某个服务名\n"
      + "          headers:          #header匹配\n"
      + "            xx:\n"
      + "              regex: xx\n"
      + "              caseInsensitive: false # 是否区分大小写，默认为false，区分大小写\n"
      + "            xxx:\n"
      + "              exact: xx\n"
      + "        route:\n"
      + "          - weight: 1\n"
      + "            tags:\n"
      + "              version: 1\n"
      + "              app: a";

  @Test
  public void testMainFilter() {
    String targetServiceName = "test_server";
    TestDistributer TestDistributer = new TestDistributer();
    List<ServiceIns> serverlist = new ArrayList<>();
    serverlist.add(new ServiceIns("0"));
    new Expectations() {
      {
        DynamicPropertyFactory dpf = DynamicPropertyFactory.getInstance();
        result = dpf;
        //todo: can not mock runnable
        dpf.getStringProperty(anyString, null, (Runnable) any);
        result = ruleStr;
      }
    };
    List<ServiceIns> serverList = CanaryFilter
        .getFilteredListOfServers(serverlist, targetServiceName, new HashMap(),
            TestDistributer);
    return;
  }

  public
  class ServiceIns extends Server {

    public ServiceIns(String id) {
      super(id);
    }

    public String getVersion() {
      return "11";
    }

    public String getServerName() {
      return "xx";
    }

    public Map<String, String> getTags() {
      Map<String, String> map = new HashMap();
      map.put("xxx", "xx");
      map.put("xx", "xx");
      return map;
    }
  }

  class TestDistributer extends AbstractCanaryDistributer<ServiceIns, ServiceIns> {

    public TestDistributer() {
      init(a -> a, ServiceIns::getVersion, ServiceIns::getServerName, ServiceIns::getTags);
    }
  }
}
