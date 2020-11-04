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

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.huaweicloud.common.log.StructuredLog;
import com.huaweicloud.common.log.logConstantValue;
import com.huaweicloud.config.ConfigWatch;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.junit.Test;

/**
 * @Author wangqijun
 * @Date 20:42 2019-10-27
 **/
public class ConfigWatchTest {


  @Test
  public void isRunning() {
    ConfigWatch configWatch = new ConfigWatch();
    assertEquals(configWatch.isRunning(), false);
  }

  @Test
  public void isAutoStartup() {
    ConfigWatch configWatch = new ConfigWatch();
    assertEquals(configWatch.isAutoStartup(), true);
  }

  @Test
  public void testLog() throws JsonProcessingException {
    StructuredLog log = new StructuredLog();
    long curTime = System.currentTimeMillis();
    log.setLevel(logConstantValue.LOG_LEVEL_INFO);
    log.setModule(logConstantValue.MODULE_CONFIG);
    log.setEvent(logConstantValue.EVENT_POLL);
    log.setTimestamp(curTime);
    log.setMsg("");
    log.setService("price");
    log.setVersion("1.0.1");
    log.setEnv("");
    log.setApp("app");
    log.setInstance("13245312564");
    log.setSystem(logConstantValue.SYSTEM_SERVICECOMB);
    String jasonDataLog  = JsonUtils.OBJ_MAPPER.writeValueAsString(log);
    System.out.println(jasonDataLog);
  }
}