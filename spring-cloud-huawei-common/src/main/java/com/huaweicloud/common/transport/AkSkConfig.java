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

import com.huawei.paas.foundation.auth.signer.utils.SignerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @Author wangqijun
 * @Date 19:07 2019-09-05
 **/
public class AkSkConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(AkSkConfig.class);

  private boolean enabled = false;

  private String accessKey;

  private String secretKey;

  private String akskCustomCipher = "default";

  private String project;

  public boolean isAkSkEmpty() {
    return StringUtils.isEmpty(accessKey) || StringUtils.isEmpty(secretKey);
  }

  public boolean isProjectEmpty() {
    return StringUtils.isEmpty(project);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public AkSkConfig setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public AkSkConfig setAccessKey(String accessKey) {
    this.accessKey = accessKey;
    return this;
  }

  public String getSecretKey() {
    if ("ShaAKSKCipher".equalsIgnoreCase(this.akskCustomCipher)) {
      return this.secretKey;
    }
    try {
      return SignerUtils.sha256Encode(this.secretKey, this.accessKey);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return null;
    }
  }

  public AkSkConfig setSecretKey(String secretKey) {
    this.secretKey = secretKey;
    return this;
  }

  public String getAkskCustomCipher() {
    return akskCustomCipher;
  }

  public AkSkConfig setAkskCustomCipher(String akskCustomCipher) {
    this.akskCustomCipher = akskCustomCipher;
    return this;
  }

  public String getProject() {
    return project;
  }

  public AkSkConfig setProject(String project) {
    this.project = project;
    return this;
  }
}
