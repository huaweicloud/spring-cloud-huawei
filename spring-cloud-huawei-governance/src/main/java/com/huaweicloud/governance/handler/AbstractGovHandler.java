package com.huaweicloud.governance.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.context.event.EventListener;

import com.huaweicloud.config.ConfigRefreshEvent;

public abstract class AbstractGovHandler<T> implements GovHandler {

  protected Map<String, T> map = new HashMap<>();

  // 并发考虑
  protected <R> T getActuator(String key, R policy, Function<R, T> func) {
    T processor = map.get(key);
    if (processor == null) {
      processor = func.apply(policy);
      map.put(key, processor);
    }
    return processor;
  }

  @EventListener
  void handleConfigChange(ConfigRefreshEvent event) {
    for (String s : event.getChange()) {
      for (String s1 : map.keySet()) {
        if (s.equals(s1)) {
          map.remove(s1);
        }
      }
    }
  }
}
