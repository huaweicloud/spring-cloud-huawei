package com.huaweicloud.governance.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.common.gov.GovConfigChangeConverter;

public abstract class AbstractGovHandler<T> implements GovHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGovHandler.class);

  protected Map<String, T> map = new HashMap<>();

  @Autowired(required = false)
  private GovConfigChangeConverter govConfigChangeConverter;

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
    if (govConfigChangeConverter == null) {
      return;
    }
    Set<String> set = govConfigChangeConverter.convert(event);
    if (CollectionUtils.isEmpty(set)) {
      return;
    }
    for (String s : set) {
      for (String s1 : map.keySet()) {
        if (s.equals(s1)) {
          map.remove(s1);
        }
      }
    }
  }
}
