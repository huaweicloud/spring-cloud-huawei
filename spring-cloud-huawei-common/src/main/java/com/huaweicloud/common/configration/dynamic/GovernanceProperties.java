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

package com.huaweicloud.common.configration.dynamic;

public class GovernanceProperties {
  public static final int WEB_FILTER_BULKHEAD_ORDER = -40000;

  public static final int WEB_FILTER_CIRCUIT_BREAKER_ORDER = -45000;

  public static final int WEB_FILTER_RATE_LIMITING_ORDER = -50000;

  public static final int WEB_FILTER_IDENTIFIER_RATE_LIMITING_ORDER = -55000;

  public static final int WEB_FILTER_INVOCATION_METRICS_ORDER = -60000;

  public static final String PREFIX = "spring.cloud.servicecomb";

  public static final String GATEWAY_GOVERNANCE_ENABLED = PREFIX + "." + "gateway.governance.enabled";

  public static final String GATEWAY_RATE_LIMITING_ENABLED = PREFIX + "." + "gateway.rateLimiting.enabled";

  public static final String GATEWAY_IDENTIFIER_RATE_LIMITING_ENABLED =
      PREFIX + "." + "gateway.identifierRateLimiting.enabled";

  public static final String GATEWAY_FAULT_INJECTION_ENABLED = PREFIX + "." + "gateway.faultInjection.enabled";

  public static final String GATEWAY_INSTANCE_ISOLATION_ENABLED = PREFIX + "." + "gateway.instanceIsolation.enabled";

  public static final String GATEWAY_INSTANCE_BULKHEAD_ENABLED = PREFIX + "." + "gateway.instanceBulkhead.enabled";

  public static final String WEBMVC_RATE_LIMITING_ENABLED =
      PREFIX + "." + "webmvc.rateLimiting.enabled";

  public static final String WEBMVC_BULKHEAD_ENABLED =
      PREFIX + "." + "webmvc.bulkhead.enabled";

  public static final String WEBMVC_CIRCUIT_BREAKER_ENABLED =
      PREFIX + "." + "webmvc.circuitBreaker.enabled";

  public static final String WEBMVC_IDENTIFIER_RATE_LIMITING_ENABLED =
      PREFIX + "." + "webmvc.identifierRateLimiting.enabled";

  public static final String WEBMVC_PUBLICKEY_CONSUMER_ENABLED = PREFIX + "." + "webmvc.publicKey.consumerEnabled";

  public static final String WEBMVC_PUBLICKEY_PROVIDER_ENABLED = PREFIX + "." + "webmvc.publicKey.providerEnabled";

  public static final String WEBMVC_PUBLICKEY_ACCSSCONTROL = PREFIX + "." + "webmvc.public-key.access-control";

  public static class Gateway {
    private RateLimiting rateLimiting = new RateLimiting();

    private IdentifierRateLimiting identifierRateLimiting = new IdentifierRateLimiting();

    private InvocationMetrics invocationMetrics = new InvocationMetrics();

    public RateLimiting getRateLimiting() {
      return rateLimiting;
    }

    public void setRateLimiting(RateLimiting rateLimiting) {
      this.rateLimiting = rateLimiting;
    }

    public IdentifierRateLimiting getIdentifierRateLimiting() {
      return identifierRateLimiting;
    }

    public void setIdentifierRateLimiting(
        IdentifierRateLimiting identifierRateLimiting) {
      this.identifierRateLimiting = identifierRateLimiting;
    }

    public InvocationMetrics getInvocationMetrics() {
      return invocationMetrics;
    }

    public void setInvocationMetrics(
        InvocationMetrics invocationMetrics) {
      this.invocationMetrics = invocationMetrics;
    }
  }

  public static class Webmvc {
    private RateLimiting rateLimiting = new RateLimiting();

    private IdentifierRateLimiting identifierRateLimiting = new IdentifierRateLimiting();

    private Bulkhead bulkhead = new Bulkhead();

    private CircuitBreaker circuitBreaker = new CircuitBreaker();

    private InvocationMetrics invocationMetrics = new InvocationMetrics();

    public RateLimiting getRateLimiting() {
      return rateLimiting;
    }

    public void setRateLimiting(RateLimiting rateLimiting) {
      this.rateLimiting = rateLimiting;
    }

    public IdentifierRateLimiting getIdentifierRateLimiting() {
      return identifierRateLimiting;
    }

    public void setIdentifierRateLimiting(
        IdentifierRateLimiting identifierRateLimiting) {
      this.identifierRateLimiting = identifierRateLimiting;
    }

    public InvocationMetrics getInvocationMetrics() {
      return invocationMetrics;
    }

    public void setInvocationMetrics(
        InvocationMetrics invocationMetrics) {
      this.invocationMetrics = invocationMetrics;
    }

    public Bulkhead getBulkhead() {
      return bulkhead;
    }

    public void setBulkhead(Bulkhead bulkhead) {
      this.bulkhead = bulkhead;
    }

    public CircuitBreaker getCircuitBreaker() {
      return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
      this.circuitBreaker = circuitBreaker;
    }
  }

  public static class RateLimiting {
    private int order = WEB_FILTER_RATE_LIMITING_ORDER;

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }
  }

  public static class CircuitBreaker {
    private int order = WEB_FILTER_CIRCUIT_BREAKER_ORDER;

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }
  }

  public static class Bulkhead {
    private int order = WEB_FILTER_BULKHEAD_ORDER;

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }
  }

  public static class IdentifierRateLimiting {
    private int order = WEB_FILTER_IDENTIFIER_RATE_LIMITING_ORDER;

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }
  }

  public static class InvocationMetrics {
    private int order = WEB_FILTER_INVOCATION_METRICS_ORDER;

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }
  }

  private Gateway gateway = new Gateway();

  private Webmvc webmvc = new Webmvc();

  public Gateway getGateway() {
    return gateway;
  }

  public void setGateway(Gateway gateway) {
    this.gateway = gateway;
  }

  public Webmvc getWebmvc() {
    return webmvc;
  }

  public void setWebmvc(Webmvc webmvc) {
    this.webmvc = webmvc;
  }
}
