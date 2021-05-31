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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.model.RbacTokenRequest;
import org.apache.servicecomb.service.center.client.model.RbacTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.huaweicloud.common.disovery.ServiceCenterUtils;

public class RBACRequestAuthHeaderProvider implements AuthHeaderProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(RBACRequestAuthHeaderProvider.class);

  public static final String CACHE_KEY = "token";

  public static final String AUTH_HEADER = "Authorization";

  private static final long TOKEN_REFRESH_TIME_IN_SECONDS = 20 * 60 * 1000;

  private ServiceCombDiscoveryProperties discoveryProperties;

  private ServiceCombSSLProperties serviceCombSSLProperties;

  private ServiceCombRBACProperties serviceCombRBACProperties;

  private ExecutorService executorService = Executors.newFixedThreadPool(1, t -> new Thread(t, "rbac-executor"));

  private LoadingCache<String, String> cache;

  public RBACRequestAuthHeaderProvider(ServiceCombDiscoveryProperties discoveryProperties,
      ServiceCombSSLProperties serviceCombSSLProperties,
      ServiceCombRBACProperties serviceCombRBACProperties) {
    this.discoveryProperties = discoveryProperties;
    this.serviceCombSSLProperties = serviceCombSSLProperties;
    this.serviceCombRBACProperties = serviceCombRBACProperties;

    if (enabled()) {
      executorService = Executors.newFixedThreadPool(1, t -> new Thread(t, "rbac-executor"));
      cache = CacheBuilder.newBuilder()
          .maximumSize(1)
          .refreshAfterWrite(refreshTime(), TimeUnit.MILLISECONDS)
          .build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
              return createHeaders();
            }

            @Override
            public ListenableFuture<String> reload(String key, String oldValue) throws Exception {
              return Futures.submit(() -> createHeaders(), executorService);
            }
          });
    }
  }

  protected String createHeaders() {
    LOGGER.info("start to create rbac headers");
    RbacTokenResponse response = callCreateHeaders();
    if (response.getStatusCode() == HttpStatus.SC_OK) {
      return response.getToken();
    }
    LOGGER.warn("create rbac headers failed, status code=" + response.getStatusCode());
    return null;
  }

  protected RbacTokenResponse callCreateHeaders() {
    ServiceCenterClient serviceCenterClient = ServiceCenterUtils
        .serviceCenterClient(discoveryProperties, serviceCombSSLProperties,
            Collections.emptyList());
    RbacTokenRequest request = new RbacTokenRequest();
    request.setName(serviceCombRBACProperties.getName());
    request.setPassword(serviceCombRBACProperties.getPassword());

    return serviceCenterClient.queryToken(request);
  }

  protected long refreshTime() {
    return TOKEN_REFRESH_TIME_IN_SECONDS;
  }

  @Override
  public Map<String, String> authHeaders() {
    if (!enabled()) {
      return Collections.emptyMap();
    }

    try {
      String header = cache.get(CACHE_KEY);
      if (!StringUtils.isEmpty(header)) {
        Map<String, String> tokens = new HashMap<>(1);
        tokens.put(AUTH_HEADER, "Bearer " + header);
        return tokens;
      }
    } catch (Exception e) {
      LOGGER.error("Get auth headers failed", e);
    }
    return Collections.emptyMap();
  }

  private boolean enabled() {
    return !StringUtils.isEmpty(serviceCombRBACProperties.getName()) && !StringUtils
        .isEmpty(serviceCombRBACProperties.getPassword());
  }
}
