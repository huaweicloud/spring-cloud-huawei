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
package com.huaweicloud.governance.properties;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.huaweicloud.governance.policy.RateLimitingPolicy;

@Component
@ConfigurationProperties("servicecomb")
public class RateLimitProperties implements GovProperties<RateLimitingPolicy> {

  Map<String, String> rateLimiting;

  @Autowired
  SerializeCache<RateLimitingPolicy> cache;

  public Map<String, String> getRateLimiting() {
    return rateLimiting;
  }

  public void setRateLimiting(Map<String, String> rateLimiting) {
    this.rateLimiting = rateLimiting;
  }

  public Map<String, RateLimitingPolicy> covert() {
    return cache.get(rateLimiting, RateLimitingPolicy.class);
  }
}
