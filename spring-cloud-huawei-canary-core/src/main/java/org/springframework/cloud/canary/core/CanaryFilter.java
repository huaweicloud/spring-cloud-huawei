package org.springframework.cloud.canary.core;

import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.canary.core.cache.CanaryRuleCache;
import org.springframework.cloud.canary.core.distribute.CanaryDistributer;
import org.springframework.cloud.canary.core.match.CanaryRuleMatcher;
import org.springframework.cloud.canary.core.model.PolicyRuleItem;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2019/10/16
 **/
public class CanaryFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanaryFilter.class);

    public static <T extends Server, E> List<T> getFilteredListOfServers(List<T> list, String targetServiceName, String currentServiceName,
                                                                         Map<String, String> headers, CanaryDistributer<T, E> distributer) {
        LOGGER.debug("start canary release");
        if (CollectionUtils.isEmpty(list)) {
            LOGGER.debug("start canary release list is null");
            return list;
        }
        if (headers == null) {
            LOGGER.warn("headers is null provide when canary release");
            headers = new HashMap<>();
        }
        /**
         * 1.初始化--进行cache缓存
         */
        LOGGER.debug("start canary release init");
        // 初始化
        if (!CanaryRuleCache.doInit(targetServiceName, currentServiceName)) {
            LOGGER.debug("canary release init failed");
            return list;
        }
        LOGGER.debug("canary release init success");
        /**
         * 2.match--拿到invoke相关信息 (header)
         */
        //这里匹配到唯一的rule
        PolicyRuleItem invokeRule = CanaryRuleMatcher.getInstance().match(targetServiceName, headers);
        LOGGER.info("canary release match rule success");

        if (invokeRule == null) {
            LOGGER.debug("canary release match rule failed");
            return list;
        }
        /**
         * 3.distribute--拿到server list选择endpoint进行流量分配
         */
        return distributer.distribut(targetServiceName, list, invokeRule);
    }
}
