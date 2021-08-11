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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.huaweicloud.common.util.Cipher;
import com.huaweicloud.common.util.DefaultCipher;
import com.huaweicloud.common.util.SecretUtil;
import com.huaweicloud.common.util.ShaAKSKCipher;

@ConfigurationProperties("spring.cloud.servicecomb.credentials")
public class ServiceCombAkSkProperties {

  @Autowired(required = false)
  private List<Cipher> ciphers;

  private String enable;

  private boolean enabled = false;

  private String accessKey;

  private String secretKey;

  @Value("#{'${spring.cloud.servicecomb.credentials.cipher:${spring.cloud.servicecomb.credentials.akskCustomCipher:default}}'}")
  private String cipher;

  private String project = "default";

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
    String decodedSecretKey = new String(DefaultCipher.findCipher(ciphers, this.cipher).
        decrypt(this.secretKey.toCharArray()));

    if (ShaAKSKCipher.CIPHER_NAME.equalsIgnoreCase(this.cipher)) {
      return decodedSecretKey;
    }

    return SecretUtil.sha256Encode(decodedSecretKey, this.accessKey);
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getCipher() {
    return cipher;
  }

  public void setCipher(String cipher) {
    this.cipher = cipher;
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

  public boolean isEmpty() {
    return getAccessKey() == null || getSecretKey() == null;
  }
}
