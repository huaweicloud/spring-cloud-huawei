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

package com.huaweicloud.nacos.config;

public class NacosConfigConst {
  public static final String SECURITY_CONFIG_DATA_ID_PREFIX = "cse-app-security-";

  public static final String DEFAULT_CONFIG_FILE_EXTENSION = "yaml";

  public static final String COMMAS = ",";

  public static final String STATUS_UP = "UP";

  public static final String RETRY_MASTER_ENABLED = "spring.cloud.nacos.config.retryMasterServerEnabled";

  public static final String LABEL_ROUTER_DATA_ID_PREFIX = "cse-label-route-";

  public static final String ROUTER_CONFIG_DEFAULT_LOAD_ENABLED
      = "spring.cloud.nacos.config.routerConfigDefaultLoadEnabled";

  public static final String SECURITY_CONFIG_DEFAULT_LOAD_ENABLED
          = "spring.cloud.nacos.config.securityConfigDefaultLoadEnabled";

  public static final String ROUTER_HEADER_CONTEXT_CONFIG_DATA_ID = "cse-router-header-context-mapper.yaml";
}
