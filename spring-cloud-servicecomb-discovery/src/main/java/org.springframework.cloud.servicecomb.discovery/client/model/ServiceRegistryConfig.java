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

package org.springframework.cloud.servicecomb.discovery.client.model;

/**
 * @Author wangqijun
 * @Date 11:34 2019-07-09
 **/
public final class ServiceRegistryConfig {
  public static final String DEFAULT_API_VERSION = "v4";

  public static final String DEFAULT_PROJECT = "default";

  public static final String TENANT_NAME = "servicecomb.config.client.tenantName";

  public static final String DOMAIN_NAME = "servicecomb.config.client.domainName";

  public static final String NO_TENANT = "default";

  public static final String DEFAULT_APPID = "default";

  public static final String DEFAULT_CALL_VERSION = "latest";

  public static final int DEFAULT_HEALTHCHECK_INTERVAL = 10;

  public static final int DEFAULT_DELAY_TIME = 10 * 1000;
}
