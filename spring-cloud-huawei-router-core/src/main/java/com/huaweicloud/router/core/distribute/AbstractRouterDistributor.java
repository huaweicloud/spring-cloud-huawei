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
package com.huaweicloud.router.core.distribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.common.util.VersionCompareUtil;
import com.huaweicloud.router.core.cache.RouterRuleCache;
import com.huaweicloud.router.core.model.PolicyRuleItem;
import com.huaweicloud.router.core.model.RouteItem;
import com.huaweicloud.router.core.model.TagItem;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServer;
import com.netflix.loadbalancer.Server;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public abstract class AbstractRouterDistributor<T extends Server> implements
    RouterDistributor<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRouterDistributor.class);

  @Override
  public List<T> distribute(String targetServiceName, List<T> list, PolicyRuleItem invokeRule) {
    //初始化LatestVersion
    initLatestVersion(targetServiceName, list);

    invokeRule.check(
        RouterRuleCache.getServiceInfoCacheMap().get(targetServiceName).getLatestVersionTag());

    // 建立tag list
    Map<TagItem, List<T>> versionServerMap = getDistributList(targetServiceName, list, invokeRule);

    //如果没有匹配到合适的规则，直接返回最新版本的服务列表
    if (CollectionUtils.isEmpty(versionServerMap)) {
      LOGGER.debug("route management can not match any rule and route the latest version");
      return getLatestVersionList(list, targetServiceName);
    }

    // 分配流量，返回结果
    TagItem targetTag = getFiltedServerTagItem(invokeRule, targetServiceName);
    if (versionServerMap.containsKey(targetTag)) {
      return versionServerMap.get(targetTag);
    }
    return getLatestVersionList(list, targetServiceName);
  }

  @Override
  public void init() {
  }

  public TagItem getFiltedServerTagItem(PolicyRuleItem rule, String targetServiceName) {
    return RouterRuleCache.getServiceInfoCacheMap().get(targetServiceName)
        .getNextInvokeVersion(rule);
  }

  /**
   * 1.过滤targetService 2.返回按照version和tags分配list 这里之所以需要建立Map，而不是直接遍历List来分配是因为需要考虑 “多重匹配”
   * 因为getProperties中除了tag还有其他的无关字段
   *
   * @param serviceName
   * @param list
   * @return
   */
  private Map<TagItem, List<T>> getDistributList(String serviceName,
      List<T> list,
      PolicyRuleItem invokeRule) {
    String latestV = RouterRuleCache.getServiceInfoCacheMap().get(serviceName).getLatestVersionTag()
        .getVersion();
    Map<TagItem, List<T>> versionServerMap = new HashMap<>();
    for (T item : list) {
      ServiceCombServer server = (ServiceCombServer) item;
      MicroserviceInstance instance = server.getServiceCombServiceInstance().getMicroserviceInstance();
      if (instance.getServiceName().equals(serviceName)) {
        //最多匹配原则
        TagItem tagItem = new TagItem(instance.getVersion(), instance.getProperties());
        TagItem targetTag = null;
        int maxMatch = 0;
        for (RouteItem entry : invokeRule.getRoute()) {
          int nowMatch = entry.getTagitem().matchNum(tagItem);
          if (nowMatch > maxMatch) {
            maxMatch = nowMatch;
            targetTag = entry.getTagitem();
          }
        }
        if (invokeRule.isWeightLess() && instance.getVersion().equals(latestV)) {
          TagItem latestVTag = invokeRule.getRoute().get(invokeRule.getRoute().size() - 1)
              .getTagitem();
          if (!versionServerMap.containsKey(latestVTag)) {
            versionServerMap.put(latestVTag, new ArrayList<>());
          }
          versionServerMap.get(latestVTag).add((T) server);
        }
        if (targetTag != null) {
          if (!versionServerMap.containsKey(targetTag)) {
            versionServerMap.put(targetTag, new ArrayList<>());
          }
          versionServerMap.get(targetTag).add((T) server);
        }
      }
    }
    return versionServerMap;
  }

  public void initLatestVersion(String serviceName, List<T> list) {
    if (RouterRuleCache.getServiceInfoCacheMap().get(serviceName).getLatestVersionTag() != null) {
      return;
    }
    String latestVersion = null;
    for (T item : list) {
      ServiceCombServer server = (ServiceCombServer) item;
      MicroserviceInstance instance = server.getServiceCombServiceInstance().getMicroserviceInstance();
      if (instance.getServiceName().equals(serviceName)) {
        if (latestVersion == null || VersionCompareUtil
            .compareVersion(latestVersion, instance.getVersion()) == -1) {
          latestVersion = instance.getVersion();
        }
      }
    }
    TagItem tagitem = new TagItem(latestVersion);
    RouterRuleCache.getServiceInfoCacheMap().get(serviceName).setLatestVersionTag(tagitem);
  }

  public List<T> getLatestVersionList(List<T> list, String targetServiceName) {
    String latestV = RouterRuleCache.getServiceInfoCacheMap().get(targetServiceName)
        .getLatestVersionTag().getVersion();
    return list.stream().map(item -> (ServiceCombServer) item).filter(server ->
        server.getServiceCombServiceInstance().getMicroserviceInstance().getVersion().equals(latestV)
    ).map(item -> (T) item).collect(Collectors.toList());
  }
}
