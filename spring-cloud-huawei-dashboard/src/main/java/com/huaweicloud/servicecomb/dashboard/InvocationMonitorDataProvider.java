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
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;

import com.huaweicloud.common.configration.dynamic.DashboardProperties;
import com.huaweicloud.common.metrics.InvocationMetrics;
import com.huaweicloud.servicecomb.dashboard.model.MonitorDataProvider;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class InvocationMonitorDataProvider implements MonitorDataProvider {
  public static final String NAME_PROVIDER = "Provider.";

  private final MeterRegistry meterRegistry;

  private final ServiceCombRegistration registration;

  private final DashboardProperties dashboardProperties;

  private Map<String, GovernanceData> lastMetricsData;

  public InvocationMonitorDataProvider(MeterRegistry meterRegistry, ServiceCombRegistration registration,
      DashboardProperties dashboardProperties) {
    this.meterRegistry = meterRegistry;
    this.registration = registration;
    this.dashboardProperties = dashboardProperties;
    EventManager.register(this);
  }

  @Override
  public boolean enabled() {
    return this.dashboardProperties.isInvocationProviderEnabled();
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
  // no concurrent access
  public void extractInterfaceInfo(MonitorData monitorData) {
    List<Meter> meters = this.meterRegistry.getMeters();

    Map<String, GovernanceData> metricsData = new HashMap<>();

    for (Meter meter : meters) {
      if (!meter.getId().getName().startsWith(InvocationMetrics.METRICS_PREFIX)) {
        continue;
      }

      String name = meter.getId().getTag(InvocationMetrics.TAG_NAME);
      if (StringUtils.isNotEmpty(name)) {
        name = NAME_PROVIDER + name;
      } else {
        continue;
      }

      GovernanceData governanceData = metricsData.computeIfAbsent(name, key -> {
        GovernanceData obj = new GovernanceData();
        obj.setName(key);
        obj.setTimeInMillis(System.currentTimeMillis());
        return obj;
      });

      if (InvocationMetrics.METRICS_CALLS.equals(meter.getId().getName())) {
        Timer timer = (Timer) meter;
        if (InvocationMetrics.CALLS_TAG_SUCCESSFUL.equals(meter.getId().getTag(InvocationMetrics.TAG_KIND))) {
          governanceData.setSuccessfulCalls(timer.count());
        } else if (InvocationMetrics.CALLS_TAG_FAILED.equals(meter.getId().getTag(InvocationMetrics.TAG_KIND))) {
          governanceData.setFailedCalls(timer.count());
        } else {
          continue;
        }
        governanceData.setTotalTime(timer.totalTime(TimeUnit.MILLISECONDS) + governanceData.getTotalTime());
      }
    }

    if (lastMetricsData == null) {
      lastMetricsData = metricsData;
      return;
    }

    metricsData.forEach((k, v) -> {
      GovernanceData lastData = lastMetricsData.get(k);
      if (lastData == null) {
        return;
      }
      InterfaceInfo interfaceInfo = new InterfaceInfo();
      interfaceInfo.setName(v.getName());
      interfaceInfo.setTotal((v.getSuccessfulCalls() + v.getFailedCalls()) -
          (lastData.getSuccessfulCalls() - lastData.getFailedCalls()));
      interfaceInfo.setFailure(v.getFailedCalls() - lastData.getFailedCalls());
      interfaceInfo.setFailureRate(
          interfaceInfo.getTotal() == 0 ? 0 : interfaceInfo.getFailure() / (double) interfaceInfo.getTotal());
      interfaceInfo.setQps(interfaceInfo.getTotal() * 1000 /
          (double) (v.getTimeInMillis() - lastData.getTimeInMillis()));
      interfaceInfo.setLatency(
          doubleToInt(interfaceInfo.getTotal() > 0d ?
              (v.getTotalTime() - lastData.getTotalTime()) / interfaceInfo.getTotal()
              : 0d));
      monitorData.addInterfaceInfo(interfaceInfo);
    });

    lastMetricsData = metricsData;
  }

  private int doubleToInt(Double d) {
    return d.intValue();
  }
}
