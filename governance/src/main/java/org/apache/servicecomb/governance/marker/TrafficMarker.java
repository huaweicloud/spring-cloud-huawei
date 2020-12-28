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
package org.apache.servicecomb.governance.marker;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class TrafficMarker {

  private String services;

  private List<Matcher> matches;

  public boolean isCurrentService(String serviceName, String version) {
    if (!StringUtils.isEmpty(this.getServices())) {
      String[] services = this.getServices().split(",");
      boolean matchService = Arrays.stream(services).anyMatch(ser -> {
        String[] serAndVer = ser.split(":");
        if (serAndVer.length == 1) {
          return serviceName.equals(serAndVer[0]);
        } else if (serAndVer.length == 2) {
          return serviceName.equals(serAndVer[0]) && version.equals(serAndVer[1]);
        } else {
          return false;
        }
      });
      return true;
    }
    return true;
  }

  public String getServices() {
    return services;
  }

  public void setServices(String services) {
    this.services = services;
  }

  public List<Matcher> getMatches() {
    return matches;
  }

  public void setMatches(List<Matcher> matches) {
    this.matches = matches;
  }
}
