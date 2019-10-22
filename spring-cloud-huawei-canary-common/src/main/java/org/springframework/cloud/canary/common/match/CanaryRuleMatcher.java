package org.springframework.cloud.canary.common.match;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.canary.common.cache.CanaryRuleCache;
import org.springframework.cloud.canary.common.model.PolicyRuleItem;

import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class CanaryRuleMatcher {
    @Autowired
    private CanaryHeaderFilterExt canaryHeaderFilterExt;

    private static CanaryRuleMatcher instance = new CanaryRuleMatcher();

    private CanaryRuleMatcher() {
    }

    /**
     * 匹配到合适的rule
     * 匹配规则即： source (目标服务名字)
     * sourceTags （一期先不考虑）
     * headers （匹配header字段）
     *
     * @param serviceName
     * @return
     */
    public PolicyRuleItem match(String serviceName, Map<String, String> invokeHeader) {
        if (canaryHeaderFilterExt != null) {
            invokeHeader = canaryHeaderFilterExt.doFilter(invokeHeader);
        }
        for (PolicyRuleItem rule : CanaryRuleCache.getServiceInfoCacheMap().get(serviceName).getAllrule()) {
            if (rule.getMatch().match(invokeHeader)) {
                return rule;
            }
        }
        return null;
    }

    public static CanaryRuleMatcher getInstance() {
        return instance;
    }
}
