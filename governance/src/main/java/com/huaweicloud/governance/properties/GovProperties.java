/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huaweicloud.governance.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

//TODO: 监听动态配置的变化
public abstract class GovProperties<T> implements InitializingBean {
  private static final Logger LOGGER = LoggerFactory.getLogger(GovProperties.class);

  private final Representer representer = new Representer();

  private final String configKey;

  @Autowired
  protected Environment environment;

  protected Map<String, T> parsedEntity;

  protected GovProperties(String key) {
    configKey = key;
    representer.getPropertyUtils().setSkipMissingProperties(true);
  }

  @Override
  public void afterPropertiesSet() {
    parsedEntity = covert(readPropertiesFromPrefix());
  }

  private Map<String, String> readPropertiesFromPrefix() {
    Set<String> allKeys = getAllKeys(environment);
    Map<String, String> result = new HashMap<>();
    allKeys.forEach(key -> {
      if (key.startsWith(configKey + ".")) {
        result.put(key.substring(configKey.length() + 1), environment.getProperty(key));
      }
    });
    return result;
  }

  private Set<String> getAllKeys(Environment environment) {
    Set<String> allKeys = new HashSet<>();

    if (!(environment instanceof ConfigurableEnvironment)) {
      LOGGER.warn("None ConfigurableEnvironment is ignored in {}", this.getClass().getName());
      return allKeys;
    }

    ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;

    for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
      getProperties(propertySource, allKeys);
    }
    return allKeys;
  }

  private void getProperties(PropertySource<?> propertySource,
      Set<String> allKeys) {
    if (propertySource instanceof CompositePropertySource) {
      // recursively get EnumerablePropertySource
      CompositePropertySource compositePropertySource = (CompositePropertySource) propertySource;
      compositePropertySource.getPropertySources().forEach(ps -> getProperties(ps, allKeys));
      return;
    }
    if (propertySource instanceof EnumerablePropertySource) {
      EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) propertySource;
      Collections.addAll(allKeys, enumerablePropertySource.getPropertyNames());
      return;
    }

    LOGGER.warn("None EnumerablePropertySource ignored in {}, propertySourceName = [{}]", this.getClass().getName(),
        propertySource.getName());
  }

  public Map<String, T> getParsedEntity() {
    return this.parsedEntity;
  }

  protected abstract Map<String, T> covert(Map<String, String> properties);

  protected Map<String, T> parseEntity(Map<String, String> yamlEntity, Class<T> entityClass) {
    if (CollectionUtils.isEmpty(yamlEntity)) {
      return Collections.emptyMap();
    }

    Yaml entityParser = new Yaml(new Constructor(new TypeDescription(entityClass, entityClass)), representer);

    Map<String, T> resultMap = new HashMap<>();
    String classKey = entityClass.getName();
    for (Entry<String, String> entry : yamlEntity.entrySet()) {
      try {
        T marker = entityParser.loadAs(entry.getValue(), entityClass);
        resultMap.put(entry.getKey(), marker);
      } catch (YAMLException e) {
        LOGGER.error("governance config yaml is illegal : {}", e.getMessage());
      }
    }
    return resultMap;
  }
}
