package com.huaweicloud.config.client.kie;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;

public class ConfigCenterFileProcessor extends ConfigValueProcessor<Map<String, Object>> {
  @Override
  public Map<String, Object> process(Map<String, Object> source) {
    Map<String, Object> result = new HashMap<>();
    for (Entry<String, Object> entry : source.entrySet()) {
      YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
      yamlFactory.setResources(new ByteArrayResource(((String) entry.getValue()).getBytes()));
      result.putAll(toMap("", yamlFactory.getObject()));
    }
    return result;
  }
}
