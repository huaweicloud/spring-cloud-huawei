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

package com.huaweicloud.servicecomb.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.dashboard.client.model.InterfaceInfo;
import org.apache.servicecomb.dashboard.client.model.MonitorData;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.governance.properties.CircuitBreakerProperties;
import org.apache.servicecomb.governance.properties.InstanceIsolationProperties;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;

import com.huaweicloud.common.configration.dynamic.DashboardProperties;
import com.huaweicloud.servicecomb.dashboard.model.MonitorDataProvider;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Monitor data based on governance module.
 */
public class GovernanceMonitorDataProvider implements MonitorDataProvider {
  public static final String NAME_PROVIDER = "Provider";

  public static final String NAME_CONSUMER = "Consumer";

  private final MeterRegistry meterRegistry;

  private final ServiceCombRegistration registration;

  private final DashboardProperties dashboardProperties;

  public GovernanceMonitorDataProvider(MeterRegistry meterRegistry, ServiceCombRegistration registration,
      DashboardProperties dashboardProperties) {
    this.meterRegistry = meterRegistry;
    this.registration = registration;
    this.dashboardProperties = dashboardProperties;
    EventManager.register(this);
  }

  @Override
  public boolean enabled() {
    return this.dashboardProperties.isGovernanceProviderEnabled();
  }

  @Override
  public String getURL() {
    return String.format("/v2/default/csemonitor/metric?service=%s",
        this.registration.getMicroservice().getServiceName());
  }

  @Override
  public Microservice getMicroservice() {
    return this.registration.getMicroservice();
  }

  @Override
  public MicroserviceInstance getMicroserviceInstance() {
    return this.registration.getMicroserviceInstance();
  }

  @Override
  public void extractInterfaceInfo(MonitorData monitorData) {
    List<Meter> meters = this.meterRegistry.getMeters();

    Map<String, GovernanceData> metricsData = new HashMap<>();

    for (Meter meter : meters) {
      String name = meter.getId().getTag("name");
      if (StringUtils.isNotEmpty(name)) {
        if (name.startsWith(InstanceIsolationProperties.MATCH_INSTANCE_ISOLATION_KEY)) {
          name = name.replace(InstanceIsolationProperties.MATCH_INSTANCE_ISOLATION_KEY, NAME_CONSUMER);
        } else if (name.startsWith(CircuitBreakerProperties.MATCH_CIRCUITBREAKER_KEY)) {
          name = name.replace(CircuitBreakerProperties.MATCH_CIRCUITBREAKER_KEY, NAME_PROVIDER);
        } else {
          continue;
        }
      } else {
        continue;
      }

      GovernanceData governanceData = metricsData.computeIfAbsent(name, key -> {
        GovernanceData obj = new GovernanceData();
        obj.setName(key);
        return obj;
      });

      if ("resilience4j.circuitbreaker.calls".equals(meter.getId().getName())) {
        Timer timer = (Timer) meter;
        if ("successful".equals(meter.getId().getTag("kind"))) {
          governanceData.setSuccessfulCalls(timer.count());
        } else if ("failed".equals(meter.getId().getTag("kind"))) {
          governanceData.setFailedCalls(timer.count());
        } else if ("ignored".equals(meter.getId().getTag("kind"))) {
          governanceData.setIgnoredCalls(timer.count());
        }
        governanceData.setTotalTime(timer.totalTime(TimeUnit.MILLISECONDS) + governanceData.getTotalTime());
        continue;
      }

      if ("resilience4j.circuitbreaker.failure.rate".equals(meter.getId().getName())) {
        Gauge gauge = (Gauge) meter;
        governanceData.setFailureRate(gauge.value());
        continue;
      }

      if ("resilience4j.circuitbreaker.slow.call.rate".equals(meter.getId().getName())) {
        Gauge gauge = (Gauge) meter;
        governanceData.setSlowRate(gauge.value());
      }
    }

    metricsData.forEach((k, v) -> {
      InterfaceInfo interfaceInfo = new InterfaceInfo();
      interfaceInfo.setName(v.getName());
      interfaceInfo.setTotal(v.getSuccessfulCalls() + v.getFailedCalls() + v.getIgnoredCalls());
      interfaceInfo.setFailure(v.getFailedCalls() + v.getIgnoredCalls());
      interfaceInfo.setFailureRate(
          interfaceInfo.getTotal() == 0 ? 0 : interfaceInfo.getFailure() / (double) interfaceInfo.getTotal());
      interfaceInfo.setQps(v.getTotalTime() > 0d ? interfaceInfo.getTotal() * 1000 / v.getTotalTime() : 0d);
      interfaceInfo.setLatency(
          doubleToInt(interfaceInfo.getTotal() > 0d ? v.getTotalTime() / interfaceInfo.getTotal() : 0d));
      interfaceInfo.setCountTimeout(
          doubleToInt(interfaceInfo.getTotal() * (Math.max(v.getSlowRate(), 0d))));
      monitorData.addInterfaceInfo(interfaceInfo);
    });
  }

  private int doubleToInt(Double d) {
    return d.intValue();
  }
}
