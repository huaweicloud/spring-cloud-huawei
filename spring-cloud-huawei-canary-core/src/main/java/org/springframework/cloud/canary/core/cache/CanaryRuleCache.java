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
package org.springframework.cloud.canary.core.cache;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cloud.canary.core.model.PolicyRuleItem;
import org.springframework.cloud.canary.core.model.ServiceInfoCache;
import org.yaml.snakeyaml.Yaml;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class CanaryRuleCache {
    private static ConcurrentHashMap<String, ServiceInfoCache> serviceInfoCacheMap = new ConcurrentHashMap<>();

    private static final String ROUTE_RULE = "servicecomb.routeRule.%s";

    //todo: 这里线程安全吗
    private static Yaml yaml = new Yaml();

    /**
     * 每次序列化的过程耗费性能，这里额外缓存，配置更新时触发回调函数
     * 返回false即初始化规则失败：
     * 1. 规则解析错误
     * 2. 规则为空
     *
     * @param targetServiceName
     * @return
     */
    public static boolean doInit(String targetServiceName, String currentServerName) {
        DynamicStringProperty ruleStr = DynamicPropertyFactory.getInstance().getStringProperty(
            String.format(ROUTE_RULE, targetServiceName), null, () -> {
                serviceInfoCacheMap.get(targetServiceName)
                    .setAllrule(
                        Arrays.asList(yaml.loadAs(DynamicPropertyFactory.getInstance()
                            .getStringProperty(String.format(ROUTE_RULE, targetServiceName), null)
                            .get(), PolicyRuleItem[].class))
                    );
                serviceInfoCacheMap.get(targetServiceName).getAllrule().forEach(a ->
                    a.getRoute().forEach(b -> b.initTagItem())
                );
                serviceInfoCacheMap.get(targetServiceName).filteRule(currentServerName);
            });
        if (ruleStr.get() == null) {
            return false;
        }
        if (!serviceInfoCacheMap.containsKey(targetServiceName)) {
            serviceInfoCacheMap.put(targetServiceName, new ServiceInfoCache());
            serviceInfoCacheMap.get(targetServiceName)
                .setAllrule(Arrays.asList(yaml.loadAs(ruleStr.get(), PolicyRuleItem[].class)));
            // 这里初始化tagitem
            serviceInfoCacheMap.get(targetServiceName).getAllrule().forEach(a ->
                a.getRoute().forEach(b -> b.initTagItem())
            );
            if (serviceInfoCacheMap.get(targetServiceName).getAllrule() == null) {
                return false;
            }
            // 过滤和当前服务名相同的服务，并按照优先级排序
            serviceInfoCacheMap.get(targetServiceName).filteRule(currentServerName);
        }
        return true;
    }

    public static ConcurrentHashMap<String, ServiceInfoCache> getServiceInfoCacheMap() {
        return serviceInfoCacheMap;
    }
}
