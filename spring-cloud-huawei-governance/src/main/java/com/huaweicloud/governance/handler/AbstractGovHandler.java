package com.huaweicloud.governance.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;

public abstract class AbstractGovHandler<T> implements GovHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGovHandler.class);

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
  void handleConfigChange(ApplicationEvent event) {
    if (!event.getClass().getName().equals("com.huaweicloud.config.ConfigRefreshEvent")) {
      return;
    }
    Method method = null;
    try {
      method = event.getClass().getMethod("getChange");
      Set<String> set = (Set<String>) method.invoke(event);
      for (String s : set) {
        for (String s1 : map.keySet()) {
          if (s.equals(s1)) {
            map.remove(s1);
          }
        }
      }
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      LOGGER.error("governance unexpect error");
    }
  }
}
