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

package com.huaweicloud.servicecomb.discovery.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.huaweicloud.common.cache.RegisterCache;
import com.huaweicloud.common.log.StructuredLog;
import com.huaweicloud.common.log.logConstantValue;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;

/**
 * @Author zyl
 * @Date 16:09 2019-10-30
 **/
public class GenerateLog {

  //生成结构化日志
  public static String generateStructureLog(ServiceCombDiscoveryProperties serviceCombConfigProperties,
      String logLevel, String msg, String Event) {
    StructuredLog log = new StructuredLog();
    long curTime = System.currentTimeMillis();
    log.setLevel(logLevel);
    log.setModule(logConstantValue.MODULE_DISCOVERY);
    log.setEvent(Event);
    log.setTimestamp(curTime);
    log.setMsg(msg);
    log.setService(serviceCombConfigProperties.getServiceName());
    log.setVersion(serviceCombConfigProperties.getVersion());
    log.setEnv(serviceCombConfigProperties.getEnvironment());
    log.setApp(serviceCombConfigProperties.getAppName());
    log.setInstance(RegisterCache.getInstanceID());
    log.setSystem(logConstantValue.SYSTEM_SERVICECOMB);
    try {
      String jasonDataLog  = JsonUtils.OBJ_MAPPER.writeValueAsString(log);
      return jasonDataLog;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }
}
