/*

 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.common.configration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.configration.dynamic.DashboardProperties;
import com.huaweicloud.common.configration.dynamic.FallbackProperties;
import com.huaweicloud.common.configration.dynamic.HttpClientProperties;
import com.huaweicloud.common.configration.dynamic.LoadBalancerProperties;

@Configuration
@EnableConfigurationProperties({ContextProperties.class, LoadBalancerProperties.class,
    HttpClientProperties.class, DashboardProperties.class, FallbackProperties.class})
public class DynamicPropertiesConfiguration {
}
