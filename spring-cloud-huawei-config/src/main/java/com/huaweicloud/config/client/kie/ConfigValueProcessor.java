package com.huaweicloud.config.client.kie;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.util.StringUtils;

public abstract class ConfigValueProcessor<T> {

  abstract Map<String, Object> process(T source);

  protected Map<String, Object> toMap(String prefix, Properties properties) {
    if (properties == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> result = new HashMap<>();
    Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      Object value = properties.getProperty(key);
      if (!StringUtils.isEmpty(prefix)) {
        key = prefix + "." + key;
      }
      if (value != null) {
        result.put(key, value);
      } else {
        result.put(key, null);
      }
    }
    return result;
  }
}
