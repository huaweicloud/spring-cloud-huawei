package com.huaweicloud.governance.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
}
