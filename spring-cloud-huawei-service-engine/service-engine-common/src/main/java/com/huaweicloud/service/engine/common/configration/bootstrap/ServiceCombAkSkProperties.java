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

package com.huaweicloud.service.engine.common.configration.bootstrap;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.huaweicloud.common.util.Cipher;
import com.huaweicloud.common.util.DefaultCipher;
import com.huaweicloud.common.util.SecretUtil;
import com.huaweicloud.common.util.ShaAKSKCipher;

public class ServiceCombAkSkProperties {

  private List<Cipher> ciphers;

  private boolean enabled = false;

  private String accessKey;

  private String secretKey;

  @Value("#{'${spring.cloud.servicecomb.credentials.cipher:${spring.cloud.servicecomb.credentials.akskCustomCipher:default}}'}")
  private String cipher;

  private String project = "default";

  @Autowired
  public void setCiphers(List<Cipher> ciphers) {
    this.ciphers = ciphers;
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
