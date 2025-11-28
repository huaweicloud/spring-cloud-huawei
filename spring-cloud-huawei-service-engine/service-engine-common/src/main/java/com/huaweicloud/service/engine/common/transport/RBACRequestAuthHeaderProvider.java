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

package com.huaweicloud.service.engine.common.transport;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.http.client.event.OperationEvents;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.model.RbacTokenRequest;
import org.apache.servicecomb.service.center.client.model.RbacTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.service.engine.common.configration.bootstrap.BootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.DiscoveryBootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombRBACProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombSSLProperties;
import com.huaweicloud.service.engine.common.disovery.ServiceCenterUtils;

public class RBACRequestAuthHeaderProvider implements AuthHeaderProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(RBACRequestAuthHeaderProvider.class);

  // special token used for special conditions
  // e.g. un-authorized: will query token after token expired period
  // e.g. not found:  will query token after token expired period
  public static final String INVALID_TOKEN = "invalid";

  public static final String CACHE_KEY = "token";

  public static final String AUTH_HEADER = "Authorization";

  private static final long TOKEN_REFRESH_TIME_IN_SECONDS = 20 * 60 * 1000;

  private static final Object LOCK = new Object();

  private final ServiceCombRBACProperties serviceCombRBACProperties;

  private ExecutorService executorService;

  private LoadingCache<String, String> cache;

  private ServiceCenterClient serviceCenterClient;

  public RBACRequestAuthHeaderProvider(BootstrapProperties bootstrapProperties, Environment env) {
    DiscoveryBootstrapProperties discoveryProperties = bootstrapProperties.getDiscoveryBootstrapProperties();
    ServiceCombSSLProperties serviceCombSSLProperties = bootstrapProperties.getServiceCombSSLProperties();
    this.serviceCombRBACProperties = bootstrapProperties.getServiceCombRBACProperties();

    if (enabled()) {
      serviceCenterClient = ServiceCenterUtils.serviceCenterClient(discoveryProperties,
          serviceCombSSLProperties, Collections.emptyList(), env);
      EventManager.getEventBus().register(this);

      executorService = Executors.newFixedThreadPool(1, t -> new Thread(t, "rbac-executor"));
      cache = CacheBuilder.newBuilder()
          .maximumSize(10)
          .refreshAfterWrite(refreshTime(), TimeUnit.MILLISECONDS)
          .build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) {
              return createHeaders(key);
            }

            @Override
            public ListenableFuture<String> reload(String key, String oldValue) {
              return Futures.submit(() -> createHeaders(key), executorService);
            }
          });
    }
  }

  @Subscribe
  public void onUnAuthorizedOperationEvent(OperationEvents.UnAuthorizedOperationEvent event) {
    LOGGER.warn("address {} unAuthorized, refresh cache token!", event.getAddress());
    cache.refresh(getHostByAddress(event.getAddress()));
  }

  private static String getHostByAddress(String address) {
    try {
      URI uri = URI.create(address);
      return uri.getHost();
    } catch (Exception e) {
      LOGGER.error("get host by address [{}] error!", address, e);
      return CACHE_KEY;
    }
  }

  protected String createHeaders(String key) {
    LOGGER.info("start to create server [{}] RBAC headers", key);
    RbacTokenResponse rbacTokenResponse = callCreateHeaders(key);

    if (Status.UNAUTHORIZED.getStatusCode() == rbacTokenResponse.getStatusCode()
        || Status.FORBIDDEN.getStatusCode() == rbacTokenResponse.getStatusCode()) {
      // password wrong, do not try anymore
      LOGGER.warn("username or password may be wrong, stop trying to query tokens.");
      return INVALID_TOKEN;
    } else if (Status.NOT_FOUND.getStatusCode() == rbacTokenResponse.getStatusCode()) {
      // service center not support, do not try
      LOGGER.warn("service center do not support RBAC token, you should not config account info");
      return INVALID_TOKEN;
    } else if (Status.INTERNAL_SERVER_ERROR.getStatusCode() == rbacTokenResponse.getStatusCode()) {
      // return null for server_error, so the token information can be re-fetched on the next call.
      // It will prompt 'CacheLoader returned null for key xxx'
      LOGGER.warn("service center query RBAC token error!");
      return null;
    }

    LOGGER.info("refresh server [{}] token successfully {}", key, rbacTokenResponse.getStatusCode());
    return rbacTokenResponse.getToken();
  }

  protected RbacTokenResponse callCreateHeaders(String host) {
    RbacTokenRequest request = new RbacTokenRequest();
    request.setName(serviceCombRBACProperties.getName());
    request.setPassword(serviceCombRBACProperties.getPassword());
    try {
      return serviceCenterClient.queryToken(request, host);
    } catch (Exception e) {
      LOGGER.error("query token from server [{}] error!", host, e);
    }
    RbacTokenResponse response = new RbacTokenResponse();
    response.setStatusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    return response;
  }

  protected long refreshTime() {
    return TOKEN_REFRESH_TIME_IN_SECONDS;
  }

  /**
   * Retrieve the corresponding engine token cache information based on the host in the request
   * to resolve cross-engine authentication issues in dual-engine disaster recovery scenarios.
   *
   * @param host host
   * @return token info
   */
  @Override
  public Map<String, String> authHeaders(String host) {
    if (!enabled()) {
      return Collections.emptyMap();
    }
    String address = host;
    if (StringUtils.isEmpty(address)) {
      address = CACHE_KEY;
    }
    synchronized (LOCK) {
      try {
        String header = cache.get(address);
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
  }

  private boolean enabled() {
    return !StringUtils.isEmpty(serviceCombRBACProperties.getName()) && !StringUtils
        .isEmpty(serviceCombRBACProperties.getPassword());
  }
}
