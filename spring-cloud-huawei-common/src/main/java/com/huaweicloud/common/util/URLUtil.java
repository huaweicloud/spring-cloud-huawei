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

package com.huaweicloud.common.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @Author wangqijun
 * @Date 17:26 2019-08-07
 **/
public class URLUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(URLUtil.class);

  private static final String SCHEMA_SEPRATOR = "://";

  private static final String SYSTEM_KEY_BOTH = "PAAS_CSE_ENDPOINT";

  private static final String SYSTEM_KEY_SERVICE_CENTER = "PAAS_CSE_SC_ENDPOINT";

  private static final String SYSTEM_KEY_CONFIG_CENTER = "PAAS_CSE_CC_ENDPOINT";

  /**
   * Convert url to http or https
   *
   * @param restUrl
   * @param scheme
   * @return
   */
  public static String transform(String restUrl, String scheme) {
    if (restUrl == null) {
      return null;
    }
    return scheme + SCHEMA_SEPRATOR
        + restUrl.substring(restUrl.indexOf(SCHEMA_SEPRATOR) + SCHEMA_SEPRATOR.length());
  }

  /**
   * Compare two urls, if the domain and port are the same, they are considered equal
   *
   * @param url1
   * @param url2
   * @return
   */
  public static boolean isEquals(String url1, String url2) {
    if (url1 == null || url2 == null) {
      return false;
    }
    String url1ComparePart = url1
        .substring(url1.indexOf(SCHEMA_SEPRATOR) + SCHEMA_SEPRATOR.length());
    String url2ComparePart = url2
        .substring(url2.indexOf(SCHEMA_SEPRATOR) + SCHEMA_SEPRATOR.length());
    return removeSlash(url1ComparePart).equals(removeSlash(url2ComparePart));
  }

  private static String removeSlash(String url) {
    if (url.endsWith("/")) {
      String result = url.substring(0, url.length() - 1);
      return result;
    }
    return url;
  }

  public static List<String> dealMutiUrl(String urls) {
    List<String> urlList = new ArrayList<>();
    if (urls != null && urls.indexOf(",") > 0) {
      for (String url : urls.split(",")) {
        if (url != null && !url.isEmpty()) {
          urlList.add(url);
        }
      }
    } else {
      urlList.add(urls);
    }
    return urlList;
  }

  public static List<String> getEnvConfigUrl() {
    SystemConfiguration sysConfig = new SystemConfiguration();
    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();
    String sysURL = sysConfig.getString(SYSTEM_KEY_CONFIG_CENTER);
    String envURL = envConfig.getString(SYSTEM_KEY_CONFIG_CENTER);
    if (StringUtils.isEmpty(sysURL) && StringUtils.isEmpty(envURL)) {
      sysURL = sysConfig.getString(SYSTEM_KEY_BOTH);
      envURL = envConfig.getString(SYSTEM_KEY_BOTH);
    }
    return StringUtils.isEmpty(sysURL) ? dealMutiUrl(envURL) : dealMutiUrl(sysURL);
  }

  public static List<String> getEnvServerURL() {
    SystemConfiguration sysConfig = new SystemConfiguration();
    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();
    String sysURL = sysConfig.getString(SYSTEM_KEY_SERVICE_CENTER);
    String envURL = envConfig.getString(SYSTEM_KEY_SERVICE_CENTER);
    if (StringUtils.isEmpty(sysURL) && StringUtils.isEmpty(envURL)) {
      sysURL = sysConfig.getString(SYSTEM_KEY_BOTH);
      envURL = envConfig.getString(SYSTEM_KEY_BOTH);
    }
    return StringUtils.isEmpty(sysURL) ? dealMutiUrl(envURL) : dealMutiUrl(sysURL);
  }

}
