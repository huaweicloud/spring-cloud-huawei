package org.springframework.cloud.canary.common.distribute;

import org.springframework.cloud.canary.common.model.PolicyRuleItem;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public interface CanaryDistributer<T, E> {
    void init(Function<T, E> getIns, Function<E, String> getVersion,
              Function<E, String> getServerName,
              Function<E, Map<String, String>> getProperties);

    List<T> distribut(String targetServiceName, List<T> list, PolicyRuleItem invokeRule);
}
