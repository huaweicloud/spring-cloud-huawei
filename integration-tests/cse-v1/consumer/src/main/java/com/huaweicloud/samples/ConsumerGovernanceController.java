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

package com.huaweicloud.samples;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "govern")
public class ConsumerGovernanceController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerGovernanceController.class);

  private final Map<String, Integer> retryTimes = new HashMap<>();

  private AtomicInteger count = new AtomicInteger(0);

  @RequestMapping("/rateLimiting")
  public String rateLimiting() {
    return "rateLimiting";
  }

  @RequestMapping("/retry")
  public String retry(HttpServletResponse response, @RequestParam(name = "invocationID") String invocationID) {
    retryTimes.putIfAbsent(invocationID, 0);
    retryTimes.put(invocationID, retryTimes.get(invocationID) + 1);

    int retry = retryTimes.get(invocationID);

    if (retry == 3) {
      return "try times: " + retry;
    }
    response.setStatus(503);
    return null;
  }

  @RequestMapping("/circuitBreaker")
  public String circuitBreaker(HttpServletResponse response) throws Exception {
    int index = count.getAndIncrement();
    LOGGER.info("circuitBreaker index {}", index);
    if (index % 3 != 0) {
      return "ok";
    }
    response.setStatus(502);
    return null;
  }

  @RequestMapping("/bulkhead")
  public String bulkhead() {
    return "bulkhead";
  }
}
