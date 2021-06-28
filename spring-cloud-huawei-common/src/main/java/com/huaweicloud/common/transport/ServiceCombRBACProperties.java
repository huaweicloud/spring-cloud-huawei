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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huaweicloud.common.util.Cipher;
import com.huaweicloud.common.util.DefaultCipher;

@ConfigurationProperties("spring.cloud.servicecomb.credentials.account")
public class ServiceCombRBACProperties {

  @Autowired(required = false)
  @JsonIgnore
  private List<Cipher> ciphers;

  @Value("#{'${spring.cloud.servicecomb.credentials.account.cipher:${spring.cloud.servicecomb.credentials.account.akskCustomCipher:default}}'}")
  @JsonIgnore
  private String cipher;

  private String name;

  private String password;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    String decodedPassWord = new String(DefaultCipher.findCipher(ciphers, this.cipher).
        decrypt(this.password.toCharArray()));
    return decodedPassWord;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getCipher() {
    return cipher;
  }

  public void setCipher(String cipher) {
    this.cipher = cipher;
  }
}
