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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.huaweicloud.router.core.model.Matcher;
import com.huaweicloud.router.core.model.PolicyRuleItem;
import com.huaweicloud.router.core.model.RouteItem;
import org.yaml.snakeyaml.Yaml;

/**
 * @Author GuoYl123
 * @Date 2019/11/4
 **/
public class SerializeTest {
  private static String allRuleList = ""
      + "        - precedence: 1 #优先级\n"
      + "          match:        #匹配策略\n"
      + "            source: xx #匹配某个服务名\n"
      + "            headers:          #header匹配\n"
      + "              q1:            \n"
      + "                regex: ww\n"
      + "                caseInsensitive: false # 是否区分大小写，默认为false，区分大小写\n"
      + "              q2:         \n"
      + "                exact: ww   \n"
      + "          route: #路由规则\n"
      + "            - weight: 11 #权重值\n"
      + "              tags:\n"
      + "                version: 11\n"
      + "                app: 11\n"
      + "        - precedence: 2\n"
      + "          match:        \n"
      + "            refer: aaa #参考某个source模板ID\n"
      + "          route:\n"
      + "            - weight: 3\n"
      + "              tags:\n"
      + "                version: 4\n"
      + "                app: 3\n";

  private static String rootItemStr = ""
      + "            - weight: 1 \n"
      + "              tags:\n"
      + "                version: 111\n"
      + "                app: 222\n";

  private static String matcherStr = ""
      + "            source: xx #匹配某个服务名\n"
      + "            headers:   #header匹配\n"
      + "              x0:            \n"
      + "                regex: xx\n"
      + "                caseInsensitive: false # 是否区分大小写，默认为false，区分大小写\n"
      + "              x1:         \n"
      + "                exact: xx  \n";

  @Test
  public void checkMatcher() {
    Yaml yaml = new Yaml();
    Matcher matcher = yaml.loadAs(matcherStr, Matcher.class);
    System.out.println("--------------");
    System.out.println(matcher);
    System.out.println("--------------");
  }

  @Test
  public void checkRouteItemList() {
    Yaml yaml = new Yaml();
    List<RouteItem> list = Arrays.asList(yaml.loadAs(rootItemStr,RouteItem[].class));
    for (RouteItem item : list) {
      System.out.println("--------------");
      System.out.println(item);
      System.out.println("--------------");
    }
  }

  @Test
  public void checkAllParam() {
    Yaml yaml = new Yaml();
    List<PolicyRuleItem> list = Arrays.asList(yaml.loadAs(allRuleList,PolicyRuleItem[].class));
    for (PolicyRuleItem item : list) {
      System.out.println("--------------");
      System.out.println(item);
      System.out.println("--------------");
    }
  }

}
