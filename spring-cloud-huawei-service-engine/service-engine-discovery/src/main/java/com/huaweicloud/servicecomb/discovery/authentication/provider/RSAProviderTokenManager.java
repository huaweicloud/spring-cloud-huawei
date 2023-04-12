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

package com.huaweicloud.servicecomb.discovery.authentication.provider;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.common.utils.RSAUtils;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.huaweicloud.servicecomb.discovery.authentication.Const;
import com.huaweicloud.servicecomb.discovery.authentication.RsaAuthenticationToken;

public class RSAProviderTokenManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSAProviderTokenManager.class);

  private final Cache<RsaAuthenticationToken, Boolean> validatedToken = CacheBuilder.newBuilder()
      .expireAfterAccess(getExpiredTime(), TimeUnit.MILLISECONDS)
      .build();

  private final AccessController accessController;

  private final ServiceCenterClient client;

  public RSAProviderTokenManager(ServiceCenterClient client, BlackWhiteListProperties blackWhiteListProperties) {
    this.client = client;
    accessController = new AccessController(blackWhiteListProperties);
  }

  public boolean valid(String token) {
    try {
      RsaAuthenticationToken rsaToken = RsaAuthenticationToken.fromStr(token);
      if (null == rsaToken) {
        LOGGER.error("token format is error, perhaps you need to set auth handler at consumer");
        return false;
      }
      if (tokenExpired(rsaToken)) {
        LOGGER.error("token is expired");
        return false;
      }

      if (validatedToken.asMap().containsKey(rsaToken)) {
        return accessController.isAllowed(getOrCreate(rsaToken.getServiceId()));
      }

      if (isValidToken(rsaToken) && !tokenExpired(rsaToken)) {
        validatedToken.put(rsaToken, true);
        return accessController.isAllowed(getOrCreate(rsaToken.getServiceId()));
      }
      return false;
    } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
      LOGGER.error("verify error", e);
      return false;
    }
  }

  public boolean isValidToken(RsaAuthenticationToken rsaToken)
      throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    String sign = rsaToken.getSign();
    String content = rsaToken.plainToken();
    String publicKey = getPublicKeyFromInstance(rsaToken.getInstanceId(), rsaToken.getServiceId());
    return RSAUtils.verify(publicKey, sign, content);
  }

  protected int getExpiredTime() {
    return 60 * 60 * 1000;
  }

  private boolean tokenExpired(RsaAuthenticationToken rsaToken) {
    long generateTime = rsaToken.getGenerateTime();
    long expired = generateTime + RsaAuthenticationToken.TOKEN_ACTIVE_TIME + 15 * 60 * 1000;
    long now = System.currentTimeMillis();
    return now > expired;
  }

  private String getPublicKeyFromInstance(String instanceId, String serviceId) {
    MicroserviceInstance instances = getOrCreate(serviceId, instanceId);
    if (instances != null) {
      return instances.getProperties().get(Const.INSTANCE_PUBKEY_PRO);
    } else {
      LOGGER.error("not instance found {}-{}, maybe attack", instanceId, serviceId);
      return "";
    }
  }

  @VisibleForTesting
  Cache<RsaAuthenticationToken, Boolean> getValidatedToken() {
    return validatedToken;
  }

  private static final Cache<String, MicroserviceInstance> instances = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();

  private static final Cache<String, Microservice> microservices = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();

  public Microservice getOrCreate(String serviceId) {
    try {
      return microservices.get(serviceId, () -> {
        Microservice microservice = client.getMicroserviceByServiceId(serviceId);
        if (microservice == null) {
          throw new IllegalArgumentException("service id not exists.");
        }
        return microservice;
      });
    } catch (ExecutionException | UncheckedExecutionException e) {
      LOGGER.error("get microservice from cache failed, {}, {}", serviceId, e.getMessage());
      return null;
    }
  }

  public MicroserviceInstance getOrCreate(String serviceId, String instanceId) {
    try {
      String key = String.format("%s@%s", serviceId, instanceId);
      return instances.get(key, () -> {
        MicroserviceInstance instance = client.getMicroserviceInstance(serviceId, instanceId);
        if (instance == null) {
          throw new IllegalArgumentException("instance id not exists.");
        }
        return instance;
      });
    } catch (ExecutionException | UncheckedExecutionException e) {
      LOGGER.error("get microservice instance from cache failed, {}, {}",
          String.format("%s@%s", serviceId, instanceId),
          e.getMessage());
      return null;
    }
  }
}
