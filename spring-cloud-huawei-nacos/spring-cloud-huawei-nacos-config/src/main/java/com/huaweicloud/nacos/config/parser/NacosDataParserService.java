/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.nacos.config.parser;

import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.nacos.config.NacosConfigConst;

public class NacosDataParserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosDataParserService.class);

  private List<PropertySourceLoader> propertySourceLoaders;

  private static final NacosDataParserService INSTANCE = new NacosDataParserService();

  private static final String DEFAULT_EXTENSION = "properties";

  private NacosDataParserService() {
    propertySourceLoaders = SpringFactoriesLoader
        .loadFactories(PropertySourceLoader.class, getClass().getClassLoader());
  }

  public static NacosDataParserService getInstance() {
    return INSTANCE;
  }

  public String getFileExtension(String fileExtension) {
    if (!StringUtils.isEmpty(fileExtension)) {
      int index = fileExtension.lastIndexOf(NacosConfigConst.DOT);
      if (index > 0 && index < fileExtension.length() - 1) {
        return fileExtension.substring(index + 1);
      }
    }
    return DEFAULT_EXTENSION;
  }

  public List<PropertySource<?>> parseNacosConfigData(String configName, String configValue, String fileExtension)
      throws IOException {
    if (StringUtils.isEmpty(configValue)) {
      LOGGER.warn("get empty config from nacos server, configName=[{}], configValue=[{}]", configName, configValue);
      return Collections.emptyList();
    }
    String realFileExtension = StringUtils.isEmpty(fileExtension) ? getFileExtension(configName) : fileExtension;
    for (PropertySourceLoader sourceLoader: propertySourceLoaders) {
      if (!hasFileExtensionLoader(sourceLoader, realFileExtension)) {
        continue;
      }
      NacosByteArrayResource byteArrayResource;
      if (sourceLoader instanceof PropertiesPropertySourceLoader) {
        byteArrayResource = new NacosByteArrayResource(covertChineseUnicode(configValue).getBytes(), configName);
      } else {
        byteArrayResource = new NacosByteArrayResource(configValue.getBytes(), configName);
      }
      byteArrayResource.setFileName(configName);
      List<PropertySource<?>> propertySourceList = sourceLoader.load(configName, byteArrayResource);
      if (CollectionUtils.isEmpty(propertySourceList)) {
        return Collections.emptyList();
      }
      return propertySourceList.stream()
          .filter(Objects::nonNull)
          .map(property -> {
            if (property instanceof EnumerablePropertySource<?>) {
              String[] propertyNames = ((EnumerablePropertySource<?>) property).getPropertyNames();
              if (propertyNames.length > 0) {
                Map<String, Object> map = new LinkedHashMap<>();
                Arrays.stream(propertyNames).forEach(name -> {
                  map.put(name, property.getProperty(name));
                });
                return new OriginTrackedMapPropertySource(property.getName(), map, true);
              }
            }
            return property;
          })
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private String covertChineseUnicode(String configValue) {
    StringBuilder sb = new StringBuilder();
    for (char ch : configValue.toCharArray()) {
      if (UnicodeBlock.of(ch) == UnicodeBlock.BASIC_LATIN || Character.isWhitespace(ch)) {
        sb.append(ch);
      } else {
        sb.append(String.format("\\u%04x", (int) ch));
      }
    }
    return sb.toString();
  }

  private boolean hasFileExtensionLoader(PropertySourceLoader sourceLoader, String fileExtension) {
    return Arrays.stream(sourceLoader.getFileExtensions())
        .anyMatch(extension -> StringUtils.endsWithIgnoreCase(fileExtension, extension));
  }
}
