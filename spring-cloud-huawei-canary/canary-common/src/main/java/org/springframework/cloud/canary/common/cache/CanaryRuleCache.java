package org.springframework.cloud.canary.common.cache;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.cloud.canary.common.model.PolicyRuleItem;
import org.springframework.cloud.canary.common.model.ServiceInfoCache;
import org.yaml.snakeyaml.Yaml;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

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
