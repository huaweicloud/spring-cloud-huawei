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

package com.huaweicloud.config.client;

import com.huaweicloud.config.ServiceCombConfigProperties;

/**
 * @Author wangqijun
 * @Date 09:59 2019-11-20
 **/
public class QueryParamUtil {

  public static String spliceDimensionsInfo(ServiceCombConfigProperties serviceCombConfigProperties) {
    String result = serviceCombConfigProperties.getServiceName() + ConfigConstants.DEFAULT_APP_SEPARATOR
        + serviceCombConfigProperties.getAppName();
    if (serviceCombConfigProperties.getVersion() != null && !serviceCombConfigProperties.getVersion().isEmpty()) {
      result = result + ConfigConstants.DEFAULT_SERVICE_SEPARATOR + serviceCombConfigProperties.getVersion();
    }
    return result;
  }
}
