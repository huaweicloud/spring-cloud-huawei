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
package com.huaweicloud.router.core.cache;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huaweicloud.router.core.model.PolicyRuleItem;
import com.huaweicloud.router.core.model.ServiceInfoCache;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class RouterRuleCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouterRuleCache.class);

  private static ConcurrentHashMap<String, ServiceInfoCache> serviceInfoCacheMap = new ConcurrentHashMap<>();

  private static final String ROUTE_RULE = "servicecomb.routeRule.%s";

  private static Interner<String> servicePool = Interners.newWeakInterner();

  /**
   * 每次序列化额外缓存，配置更新时触发回调函数 返回false即初始化规则失败： 1. 规则解析错误 2. 规则为空
   *
   * @param targetServiceName
   * @return
   */
  public static boolean doInit(String targetServiceName) {
    if (!isServerContainRule(targetServiceName)) {
      return false;
    }
    if (!serviceInfoCacheMap.containsKey(targetServiceName)) {
      synchronized (servicePool.intern(targetServiceName)) {
        if (serviceInfoCacheMap.containsKey(targetServiceName)) {
          return true;
        }
        //Yaml not thread-safe
        DynamicStringProperty ruleStr = DynamicPropertyFactory.getInstance().getStringProperty(
            String.format(ROUTE_RULE, targetServiceName), null, () -> {
              refresh(targetServiceName);
              DynamicStringProperty tepRuleStr = DynamicPropertyFactory.getInstance()
                  .getStringProperty(String.format(ROUTE_RULE, targetServiceName), null);
              addAllRule(targetServiceName, tepRuleStr.get());
            });
        return addAllRule(targetServiceName, ruleStr.get());
      }
    }
    return true;
  }

  private static boolean addAllRule(String targetServiceName, String ruleStr) {
    if (StringUtils.isEmpty(ruleStr)) {
      return false;
    }
    Yaml yaml = new Yaml();
    List<PolicyRuleItem> policyRuleItemList;
    try {
      policyRuleItemList = Arrays
          .asList(yaml.loadAs(ruleStr, PolicyRuleItem[].class));
    } catch (Exception e) {
      LOGGER.error("route management Serialization failed: {}", e.getMessage());
      return false;
    }
    if (CollectionUtils.isEmpty(policyRuleItemList)) {
      return false;
    }
    ServiceInfoCache serviceInfoCache = new ServiceInfoCache(policyRuleItemList);
    serviceInfoCacheMap.put(targetServiceName, serviceInfoCache);
    return true;
  }

  public static ConcurrentHashMap<String, ServiceInfoCache> getServiceInfoCacheMap() {
    return serviceInfoCacheMap;
  }

  /**
   * if a server don't have rule , avoid registered too many callback , it may cause memory leak
   *
   * @param targetServiceName
   * @return
   */
  public static boolean isServerContainRule(String targetServiceName) {
    DynamicStringProperty lookFor = DynamicPropertyFactory.getInstance()
        .getStringProperty(String.format(ROUTE_RULE, targetServiceName), null);
    if (StringUtils.isEmpty(lookFor.get())) {
      return false;
    }
    return true;
  }

  public static void refresh() {
    serviceInfoCacheMap = new ConcurrentHashMap<>();
    servicePool = Interners.newWeakInterner();
  }

  public static void refresh(String targetServiceName) {
    serviceInfoCacheMap.remove(targetServiceName);
  }
}
