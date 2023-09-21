/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
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
package com.huaweicloud.nacos.registry;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.configuration.EnvironmentConfiguration;

import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistrationCustomizer;

/**
 * add nacos metadata
 *
 * @author provenceee
 * @since 2023-09-19
 */
public class MetadataNacosRegistrationCustomizer implements NacosRegistrationCustomizer {
  private static final String INSTANCE_PROPS = "SERVICECOMB_INSTANCE_PROPS";

  @Override
  public void customize(NacosRegistration registration) {
    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();
    String[] instancePropArray = envConfig.getStringArray(INSTANCE_PROPS);
    if (instancePropArray.length != 0) {
      registration.getMetadata().putAll(parseProps(instancePropArray));
    }
  }

  private Map<String, String> parseProps(String... value) {
    return Arrays.stream(value).map(v -> v.split(":"))
        .filter(v -> v.length == 2)
        .collect(Collectors.toMap(v -> v[0], v -> v[1]));
  }
}
