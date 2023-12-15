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

package com.huaweicloud.governance.authentication.securityPolicy;

import java.util.ArrayList;
import java.util.List;

import com.huaweicloud.governance.authentication.MatcherUtils;

public class SecurityPolicyProperties {

  public static class ConfigurationItem {
    private String id;

    private String consumer;

    private String method;

    private String uri;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getConsumer() {
      return consumer;
    }

    public void setConsumer(String consumer) {
      this.consumer = consumer;
    }

    public String getMethod() {
      return method;
    }

    public void setMethod(String method) {
      this.method = method;
    }

    public String getUri() {
      return uri;
    }

    public void setUri(String uri) {
      this.uri = uri;
    }
  }

  public static class Action {
    private List<ConfigurationItem> allow = new ArrayList<>();

    private List<ConfigurationItem> deny = new ArrayList<>();

    public List<ConfigurationItem> getAllow() {
      return allow;
    }

    public void setAllow(
        List<ConfigurationItem> allow) {
      this.allow = allow;
    }

    public List<ConfigurationItem> getDeny() {
      return deny;
    }

    public void setDeny(
        List<ConfigurationItem> deny) {
      this.deny = deny;
    }
  }

  private String app;

  /**
   * permissive: Tolerance mode, request allow if not match policy, only reporting abnormal information
   * enforcing: Forced mode, request not allow if not match policy
   */
  private String mode;

  private String provider;

  /**
   * allow: white policy
   * deny: black policy
   */
  private Action action;

  public String getApp() {
    return app;
  }

  public void setApp(String app) {
    this.app = app;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public boolean matchAllow(String serviceName, String uri, String method) {
    if (action == null || action.allow.isEmpty()) {
      return false;
    }

    for (ConfigurationItem item : action.allow) {
      if (MatcherUtils.isPatternMatch(uri, item.getUri()) && method.equals(item.getMethod())
          && item.getConsumer().equals(serviceName)) {
        return true;
      }
    }
    return false;
  }

  public boolean matchDeny(String serviceName, String uri, String method) {
    if (action == null || action.deny.isEmpty()) {
      return false;
    }
    for (ConfigurationItem item : action.deny) {
      if (MatcherUtils.isPatternMatch(uri, item.getUri()) && method.equals(item.getMethod())
          && item.getConsumer().equals(serviceName)) {
        return true;
      }
    }
    return false;
  }
}
