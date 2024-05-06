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

package com.huaweicloud.governance.authentication;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import com.huaweicloud.governance.GovernanceConst;

public class MatcherUtils {
  public static boolean isPatternMatch(String value, String pattern) {
    if (pattern.startsWith("*") || pattern.startsWith("/*")) {
      int index = 0;
      for (int i = 0; i < pattern.length(); i++) {
        if (pattern.charAt(i) != '*' && pattern.charAt(i) != '/') {
          break;
        }
        index++;
      }
      return value.endsWith(pattern.substring(index));
    }
    if (pattern.endsWith("*")) {
      int index = pattern.length() - 1;
      for (int i = pattern.length() - 1; i >= 0; i--) {
        if (pattern.charAt(i) != '*' && pattern.charAt(i) != '/') {
          break;
        }
        index--;
      }
      return value.startsWith(pattern.substring(0, index + 1));
    }
    return value.equals(pattern);
  }

  /**
   * first determine configured non-authentication path is matched requestPath, if match not needed auth.
   * second determine whether of configured authentication path, if not configured, default all path need auth;
   * if configured, then check whether of matched requestPath, if match needed auth, otherwise not needed auth.
   *
   * @param requestPath path
   * @param env environment
   * @return notRequiredAuth
   */
  public static boolean isRequiredAuth(String requestPath, Environment env) {
    if (excludePathMatchPath(requestPath, env)) {
      return false;
    }
    return includePathMatchPath(requestPath, env);
  }

  private static boolean excludePathMatchPath(String requestPath, Environment env) {
    String excludePathPattern = env.getProperty(GovernanceConst.AUTH_API_PATH_EXCLUDE, String.class, "");
    if (StringUtils.isEmpty(excludePathPattern)) {
      excludePathPattern = env.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, "");
    }
    if (StringUtils.isEmpty(excludePathPattern)) {
      return false;
    }
    return isPathMather(requestPath, excludePathPattern);
  }

  private static boolean includePathMatchPath(String requestPath, Environment env) {
    String includePathPattern = env.getProperty(GovernanceConst.AUTH_API_PATH_INCLUDE, String.class, "");
    if (StringUtils.isEmpty(includePathPattern)) {
      return true;
    }
    return isPathMather(requestPath, includePathPattern);
  }

  private static boolean isPathMather(String requestPath, String pathPattern) {
    for (String pattern : pathPattern.split(",")) {
      if (!pattern.isEmpty() && isPatternMatch(requestPath, pattern)) {
        return true;
      }
    }
    return false;
  }
}
