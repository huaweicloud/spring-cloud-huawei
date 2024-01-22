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


/**
 * token 组成部分：
 * token: instanceId@@generateTime@randomCode@sign(instanceId@@generateTime@randomCode)
 *
 */
public class RsaAuthenticationToken {

  public static final long TOKEN_ACTIVE_TIME = 24 * 60 * 60 * 1000;

  private final String instanceId;

  private final String serviceId;

  private final long generateTime;

  private final String sign;

  private final String tokenFormat;

  private final String plainToken;

  public RsaAuthenticationToken(String instanceId, String serviceId, long generateTime,
      String randomCode, String sign) {
    this.instanceId = instanceId;
    this.generateTime = generateTime;
    this.serviceId = serviceId;
    this.sign = sign;
    this.tokenFormat = String.format("%s@%s@%s@%s@%s",
        instanceId,
        serviceId,
        generateTime,
        randomCode,
        sign);
    this.plainToken = String.format("%s@%s@%s@%s", this.instanceId, this.serviceId, this.generateTime, randomCode);
  }

  public String plainToken() {
    return this.plainToken;
  }

  public String getInstanceId() {
    return instanceId;
  }


  public long getGenerateTime() {
    return generateTime;
  }


  public String getSign() {
    return sign;
  }


  public String format() {
    return tokenFormat;
  }

  public static RsaAuthenticationToken fromStr(String token) {
    String[] tokenArr = token.split("@");
    if (tokenArr.length != 5) {
      return null;
    }
    return new RsaAuthenticationToken(tokenArr[0], tokenArr[1],
        Long.parseLong(tokenArr[2]), tokenArr[3], tokenArr[4]);
  }

  public String getServiceId() {
    return serviceId;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RsaAuthenticationToken)) {
      return false;
    }
    RsaAuthenticationToken token = (RsaAuthenticationToken) obj;
    if (!token.plainToken().equals(this.plainToken())) {
      return false;
    }
    return token.getSign().equals(this.sign);
  }

  @Override
  public int hashCode() {
    return this.plainToken().hashCode() + this.sign.hashCode();
  }
}
