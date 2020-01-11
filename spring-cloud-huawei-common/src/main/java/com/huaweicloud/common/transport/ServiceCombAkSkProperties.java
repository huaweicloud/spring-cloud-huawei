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

package com.huaweicloud.common.transport;

import com.huaweicloud.common.util.SecretUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @Author wangqijun
 * @Date 20:09 2019-09-03
 **/
@Component
@ConfigurationProperties("spring.cloud.servicecomb.credentials")
public class ServiceCombAkSkProperties {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombAkSkProperties.class);

  //dummy value for throw exception and notice
  private String enable;

  private boolean enabled = false;

  private String accessKey;

  private String secretKey;

  private String akskCustomCipher = "default";

  private String project;

  public String getEnable() {
    return enable;
  }

  public void setEnable(String enable) {
    this.enable = enable;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    if ("ShaAKSKCipher".equalsIgnoreCase(this.akskCustomCipher)) {
      return this.secretKey;
    }
    try {
      return SecretUtil.sha256Encode(this.secretKey, this.accessKey);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return null;
    }
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getAkskCustomCipher() {
    return akskCustomCipher;
  }

  public void setAkskCustomCipher(String akskCustomCipher) {
    this.akskCustomCipher = akskCustomCipher;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isAkSkEmpty() {
    return StringUtils.isEmpty(accessKey) || StringUtils.isEmpty(secretKey);
  }

  public boolean isProjectEmpty() {
    return StringUtils.isEmpty(project);
  }

}
