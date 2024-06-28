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

package com.huaweicloud.nacos.config.configdata;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.cloud.commons.ConfigDataMissingEnvironmentPostProcessor.ImportException;

public class NacosConfigImportFailureAnalyzer extends AbstractFailureAnalyzer<ImportException> {
  @Override
  protected FailureAnalysis analyze(Throwable rootFailure, ImportException cause) {
    String description;
    if (cause.missingPrefix) {
      description = "The spring.config.import property is missing a " + NacosConfigDataLocationResolver.CONFIG_PREFIX
          + " entry";
    } else {
      description = "No spring.config.import property has been defined";
    }
    String action = "Add a spring.config.import=nacos: property to your configuration.\n"
        + "\tIf configuration is not required add spring.config.import=optional:nacos: instead.\n"
        + "\tTo disable this check, set spring.cloud.nacos.config.import-check.enabled=false.";
    return new FailureAnalysis(description, action, cause);
  }
}
