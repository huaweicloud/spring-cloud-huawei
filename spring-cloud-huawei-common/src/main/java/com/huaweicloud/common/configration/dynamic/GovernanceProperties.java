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

package com.huaweicloud.common.configration.dynamic;

public class GovernanceProperties {
  public static final String PREFIX = "spring.cloud.servicecomb";

  public static final int WEB_FILTER_BULKHEAD_ORDER = -40000;

  public static final int WEB_FILTER_CIRCUIT_BREAKER_ORDER = -45000;

  public static final int WEB_FILTER_RATE_LIMITING_ORDER = -50000;

  public static final int WEB_FILTER_IDENTIFIER_RATE_LIMITING_ORDER = -55000;

  public static final int WEB_FILTER_INVOCATION_METRICS_ORDER = -60000;

  private static final int WEB_CLIENT_LOAD_BALANCE_BASE = 0;

  public static final int WEB_CLIENT_FAULT_INJECTION_ORDER =
      WEB_CLIENT_LOAD_BALANCE_BASE - 20;

  public static final int WEB_CLIENT_RETRY_ORDER =
      WEB_CLIENT_LOAD_BALANCE_BASE - 10;

  public static final int WEB_CLIENT_INSTANCE_ISOLATION_ORDER =
      WEB_CLIENT_LOAD_BALANCE_BASE + 10;

  public static final int WEB_CLIENT_INSTANCE_BULKHEAD_ORDER =
      WEB_CLIENT_LOAD_BALANCE_BASE + 20;

  public static final String WEBCLIENT_RETRY_ENABLED =
      PREFIX + "." + "webclient.retry.enabled";

  public static final String WEBCLIENT_INSTANCE_BULKHEAD_ENABLED =
      PREFIX + "." + "webclient.instanceBulkhead.enabled";

  public static final String WEBCLIENT_INSTANCE_ISOLATION_ENABLED =
      PREFIX + "." + "webclient.instanceIsolation.enabled";

  public static final String WEBCLIENT_FAULT_INJECTION_ENABLED =
      PREFIX + "." + "webclient.faultInjection.enabled";

  public static final String GATEWAY_GOVERNANCE_ENABLED = PREFIX + "." + "gateway.governance.enabled";

  public static final String GATEWAY_RETRY_ENABLED = PREFIX + "." + "gateway.retry.enabled";

  public static final String GATEWAY_FAULT_INJECTION_ENABLED = PREFIX + "." + "gateway.faultInjection.enabled";

  public static final String GATEWAY_INSTANCE_ISOLATION_ENABLED = PREFIX + "." + "gateway.instanceIsolation.enabled";

  public static final String GATEWAY_INSTANCE_BULKHEAD_ENABLED = PREFIX + "." + "gateway.instanceBulkhead.enabled";

  public static final String WEBFLUX_RATE_LIMITING_ENABLED = PREFIX + "." + "webflux.rateLimiting.enabled";

  public static final String WEBFLUX_BULKHEAD_ENABLED = PREFIX + "." + "webflux.bulkhead.enabled";

  public static final String WEBFLUX_CIRCUIT_BREAKER_ENABLED = PREFIX + "." + "webflux.circuitBreaker.enabled";

  public static final String WEBFLUX_IDENTIFIER_RATE_LIMITING_ENABLED =
      PREFIX + "." + "webflux.identifierRateLimiting.enabled";

  public static final String WEBFLUX_CONTEXT_MAPPER_ENABLED =
      PREFIX + "." + "webflux.contextMapper.enabled";

  public static final String WEBMVC_RATE_LIMITING_ENABLED =
      PREFIX + "." + "webmvc.rateLimiting.enabled";

  public static final String WEBMVC_BULKHEAD_ENABLED =
      PREFIX + "." + "webmvc.bulkhead.enabled";

  public static final String WEBMVC_CIRCUIT_BREAKER_ENABLED =
      PREFIX + "." + "webmvc.circuitBreaker.enabled";

  public static final String WEBMVC_IDENTIFIER_RATE_LIMITING_ENABLED =
      PREFIX + "." + "webmvc.identifierRateLimiting.enabled";

  public static final String WEBMVC_CONTEXT_MAPPER_ENABLED =
      PREFIX + "." + "webmvc.contextMapper.enabled";

  public static final String WEBMVC_PUBLICKEY_CONSUMER_ENABLED = PREFIX + "." + "webmvc.publicKey.consumerEnabled";

  public static final String WEBMVC_PUBLICKEY_PROVIDER_ENABLED = PREFIX + "." + "webmvc.publicKey.providerEnabled";

  public static final String WEBMVC_PUBLICKEY_ACCSSCONTROL = PREFIX + "." + "webmvc.public-key.access-control";

  public static final String REST_TEMPLATE_RETRY_ENABLED = PREFIX + "." + "restTemplate.retry.enabled";

  public static final String REST_TEMPLATE_INSTANCE_ISOLATION_ENABLED =
      PREFIX + "." + "restTemplate.instanceIsolation.enabled";

  public static final String REST_TEMPLATE_INSTANCE_BULKHEAD_ENABLED =
      PREFIX + "." + "restTemplate.instanceBulkhead.enabled";

  public static final String WEBMVC_PUBLICKEY_SECURITY_POLICY_ENABLED =
      PREFIX + "." + "webmvc.publicKey.securityPolicyEnabled";

  public static final String WEBMVC_PUBLICKEY_ACLS = PREFIX + "." + "webmvc.public-key.acls";

  public static final String SERVICECOMB_GRASEFUL_UPPER_DOWN = PREFIX + "." + "graceful.servicecombEngine.enabled";

  public static final String NACOS_GRASEFUL_UPPER_DOWN = PREFIX + "." + "graceful.nacosEngine.enabled";

  public static final String GRASEFUL_STATUS_UPPER = "UP";

  public static final String GRASEFUL_STATUS_DOWN = "DOWN";

  public static class Gateway {
    private RateLimiting rateLimiting = new RateLimiting();

    private IdentifierRateLimiting identifierRateLimiting = new IdentifierRateLimiting();

    private Bulkhead bulkhead = new Bulkhead();

    private CircuitBreaker circuitBreaker = new CircuitBreaker();

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

  public static class Webmvc {
    private RateLimiting rateLimiting = new RateLimiting();

    private IdentifierRateLimiting identifierRateLimiting = new IdentifierRateLimiting();

    private Bulkhead bulkhead = new Bulkhead();

    private CircuitBreaker circuitBreaker = new CircuitBreaker();

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

  public static class Webclient {
    private InstanceIsolation instanceIsolation = new InstanceIsolation(WEB_CLIENT_INSTANCE_ISOLATION_ORDER);

    private InstanceBulkhead instanceBulkhead = new InstanceBulkhead(WEB_CLIENT_INSTANCE_BULKHEAD_ORDER);

    private Retry retry = new Retry(WEB_CLIENT_RETRY_ORDER);

    private FaultInjection faultInjection = new FaultInjection(WEB_CLIENT_FAULT_INJECTION_ORDER);

    public InstanceIsolation getInstanceIsolation() {
      return instanceIsolation;
    }

    public void setInstanceIsolation(
        InstanceIsolation instanceIsolation) {
      this.instanceIsolation = instanceIsolation;
    }

    public InstanceBulkhead getInstanceBulkhead() {
      return instanceBulkhead;
    }

    public void setInstanceBulkhead(
        InstanceBulkhead instanceBulkhead) {
      this.instanceBulkhead = instanceBulkhead;
    }

    public Retry getRetry() {
      return retry;
    }

    public void setRetry(Retry retry) {
      this.retry = retry;
    }

    public FaultInjection getFaultInjection() {
      return faultInjection;
    }

    public void setFaultInjection(FaultInjection faultInjection) {
      this.faultInjection = faultInjection;
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

  public static class Retry {
    private int order;

    public Retry() {

    }

    public Retry(int order) {
      this.order = order;
    }

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }
  }

  public static class InstanceIsolation {
    private int order;

    public InstanceIsolation() {

    }

    public InstanceIsolation(int order) {
      this.order = order;
    }

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }
  }

  public static class InstanceBulkhead {
    private int order;

    public InstanceBulkhead() {

    }

    public InstanceBulkhead(int order) {
      this.order = order;
    }

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }
  }

  public static class FaultInjection {
    private int order;

    public FaultInjection() {

    }

    public FaultInjection(int order) {
      this.order = order;
    }

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }
  }

  private Gateway gateway = new Gateway();

  private Webmvc webmvc = new Webmvc();

  public Webclient webclient = new Webclient();

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

  public Webclient getWebclient() {
    return webclient;
  }

  public void setWebclient(Webclient webclient) {
    this.webclient = webclient;
  }
}
