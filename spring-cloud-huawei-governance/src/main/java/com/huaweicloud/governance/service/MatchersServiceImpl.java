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
package com.huaweicloud.governance.service;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import com.huaweicloud.governance.marker.GovHttpRequest;
import com.huaweicloud.governance.properties.MatchProperties;
import com.huaweicloud.governance.marker.Matcher;
import com.huaweicloud.governance.marker.RequestProcessor;
import com.huaweicloud.governance.marker.TrafficMarker;

public class MatchersServiceImpl implements MatchersService {

  @Autowired
  private RequestProcessor requestProcessor;

  @Autowired
  private MatchProperties matchProperties;

  /**
   * @param govHttpRequest
   * @return
   */
  @Override
  public String getMatchStr(GovHttpRequest govHttpRequest) {
    Map<String, TrafficMarker> map = matchProperties.covert();
    String mark = null;
    for (Entry<String, TrafficMarker> entry : map.entrySet()) {
      boolean isMatch = true;
      for (Matcher match : entry.getValue().getMatches()) {
        if (!requestProcessor.match(govHttpRequest, match)) {
          isMatch = false;
          break;
        }
      }
      if (isMatch) {
        mark = entry.getKey();
        break;
      }
    }
    return mark;
  }
}
