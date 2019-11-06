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
package org.springframework.cloud.canary.core.distribute;

import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.canary.core.cache.CanaryRuleCache;
import org.springframework.cloud.canary.core.model.PolicyRuleItem;
import org.springframework.cloud.canary.core.model.TagItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.cloud.common.util.VersionCompareUtil;
import org.springframework.util.CollectionUtils;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class AbstractCanaryDistributer<T extends Server, E> implements CanaryDistributer<T, E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCanaryDistributer.class);

  private Function<T, E> getIns;

  private Function<E, String> getVersion;

  private Function<E, String> getServerName;

  private Function<E, Map<String, String>> getProperties;

  @Override
  public List<T> distribut(String targetServiceName, List<T> list, PolicyRuleItem invokeRule) {
    //初始化LatestVersion
    initLatestVersion(targetServiceName, list);

    LOGGER.debug("canary release initialized latest version");

    invokeRule.check(
        CanaryRuleCache.getServiceInfoCacheMap().get(targetServiceName).getLatestVersionTag());

    LOGGER.debug("canary release check weight success");

    // 建立tag list
    Map<TagItem, List<T>> versionServerMap = getDistributList(targetServiceName, list, invokeRule);

    LOGGER.debug("canary release getDistributList succeed");

    //如果没有匹配到合适的规则，直接返回最新版本的服务列表
    if (CollectionUtils.isEmpty(versionServerMap)) {
      LOGGER.debug("canary release can not match any rule and route the latest version");
      return getLatestVersionList(list, targetServiceName);
    }

    LOGGER.debug("start canary release traffic distribution");
    // 分配流量，返回结果
    return getFiltedServer(versionServerMap, invokeRule, targetServiceName);
  }

  @Override
  public void init(Function<T, E> getIns,
      Function<E, String> getVersion,
      Function<E, String> getServerName,
      Function<E, Map<String, String>> getProperties) {
    this.getIns = getIns;
    this.getVersion = getVersion;
    this.getServerName = getServerName;
    this.getProperties = getProperties;
  }

  public List<T> getFiltedServer(Map<TagItem, List<T>> allServer,
      PolicyRuleItem rule, String targetServiceName) {
    return allServer
        .get(CanaryRuleCache.getServiceInfoCacheMap().get(targetServiceName)
            .getNextInvokeVersion(rule));
  }

  /**
   * 1.过滤targetService 2.返回按照version和tags分配list
   *
   * @param serviceName
   * @param list
   * @return
   */
  private Map<TagItem, List<T>> getDistributList(String serviceName,
      List<T> list,
      PolicyRuleItem invokeRule) {
    String latestV = CanaryRuleCache.getServiceInfoCacheMap().get(serviceName).getLatestVersionTag()
        .getVersion();
    Map<TagItem, List<T>> versionServerMap = new HashMap<>();
    invokeRule.getRoute().forEach(a ->
        versionServerMap.put(a.getTagitem(), new ArrayList<>())
    );
    for (T server : list) {
      //获得目标服务
      E ms = getIns.apply(server);
      if (getServerName.apply(ms).equals(serviceName)) {
        //最多匹配原则
        TagItem tagitem = new TagItem(getVersion.apply(ms), getProperties.apply(ms));
        TagItem targetTag = null;
        int maxMatch = 0;
        for (Map.Entry<TagItem, List<T>> entry : versionServerMap.entrySet()) {
          int nowMatch = entry.getKey().matchNum(tagitem);
          if (nowMatch > maxMatch) {
            maxMatch = nowMatch;
            targetTag = entry.getKey();
          }
        }
        if (invokeRule.isWeightLess() && getVersion.apply(ms).equals(latestV)) {
          versionServerMap
              .get(invokeRule.getRoute().get(invokeRule.getRoute().size() - 1).getTagitem())
              .add(server);
        }
        if (versionServerMap.containsKey(targetTag)) {
          versionServerMap.get(targetTag).add(server);
        }
      }
    }
    for (Map.Entry<TagItem, List<T>> entry : versionServerMap.entrySet()) {
      if (entry.getValue().isEmpty()){
        versionServerMap.remove(entry.getKey());
      }
    }
    return versionServerMap;
  }

  public void initLatestVersion(String serviceName, List<T> list) {
    if (CanaryRuleCache.getServiceInfoCacheMap().get(serviceName).getLatestVersionTag() != null) {
      return;
    }
    String latestVersion = null;
    for (T server : list) {
      E ms = getIns.apply(server);
      if (getServerName.apply(ms).equals(serviceName)) {
        if (latestVersion == null || VersionCompareUtil
            .compareVersion(latestVersion, getVersion.apply(ms)) == -1) {
          latestVersion = getVersion.apply(ms);
        }
      }
    }
    TagItem tagitem = new TagItem(latestVersion);
    CanaryRuleCache.getServiceInfoCacheMap().get(serviceName).setLatestVersionTag(tagitem);
  }


  public List<T> getLatestVersionList(List<T> list, String targetServiceName) {
    String latestV = CanaryRuleCache.getServiceInfoCacheMap().get(targetServiceName)
        .getLatestVersionTag().getVersion();
    return list.stream().filter(server ->
        getVersion.apply(getIns.apply(server)).equals(latestV)
    ).collect(Collectors.toList());
  }
}
