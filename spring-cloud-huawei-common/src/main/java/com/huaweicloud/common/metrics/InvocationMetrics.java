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

package com.huaweicloud.common.metrics;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.Subscribe;
import com.huaweicloud.common.configration.dynamic.MetricsProperties;
import com.huaweicloud.common.context.InvocationFinishEvent;
import com.huaweicloud.common.context.InvocationStage;
import com.huaweicloud.common.event.EventManager;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Invocation metrics maintains metrics for `the execution of an operation` of a microservice.
 *
 * e.g. when the microservice receive a request for path `/foo/bar`, invocation metrics
 * record the successful calls, failed calls and time taken for this path.
 */
public class InvocationMetrics {
  private static final Logger LOGGER = LoggerFactory.getLogger(InvocationMetrics.class);

  public static final String TAGS_SEPARATOR = "@";

  public static final String METRICS_PREFIX = "metrics.invocation.";

  public static final String TAG_NAME = "name";

  public static final String TAG_STAGE = "stage";

  public static final String TAG_STATUS = "status";

  public static final String METRICS_CALLS = METRICS_PREFIX + "calls";

  private final MeterRegistry meterRegistry;

  private final MetricsProperties metricsProperties;

  private final Pattern includePattern;

  private final Pattern excludePattern;

  private final ConcurrentMap<String, DistributionSummary> invocationDistribution = new ConcurrentHashMap<>();

  public InvocationMetrics(MeterRegistry meterRegistry, MetricsProperties metricsProperties) {
    this.meterRegistry = meterRegistry;
    this.metricsProperties = metricsProperties;
    if (StringUtils.isNotEmpty(metricsProperties.getExcludePattern())) {
      excludePattern = Pattern.compile(metricsProperties.getIncludePattern());
    } else {
      excludePattern = null;
    }
    if (StringUtils.isNotEmpty(metricsProperties.getIncludePattern())) {
      includePattern = Pattern.compile(metricsProperties.getIncludePattern());
    } else {
      includePattern = null;
    }

    EventManager.getEventBoundedAsyncEventBus().register(this);
  }

  @Subscribe
  public void onInvocationFinishEvent(InvocationFinishEvent event) {
    InvocationStage stage = event.getInvocationStage();
    recordInvocationDistribution(stage.getId(), InvocationStage.STAGE_ALL,
        stage.getStatusCode(),
        stage.getEndTime() - stage.getBeginTime());

    stage.getStages().forEach((k, v) -> recordInvocationDistribution(stage.getId(), k,
        stage.getStatusCode(),
        v.getEndTime() - v.getBeginTime()));
  }

  @VisibleForTesting
  void recordInvocationDistribution(String id, String stage, int statusCode, double amount) {
    if (byPassMethod(id, stage, statusCode)) {
      return;
    }
    DistributionSummary summary = getOrCreateInvocationDistribution(id, stage, statusCode);
    summary.record(amount);
  }

  private boolean byPassMethod(String id, String stage, int statusCode) {
    if (excludePattern != null && excludePattern.matcher(id).matches()) {
      return true;
    }
    if (includePattern != null && !includePattern.matcher(id).matches()) {
      return true;
    }
    if (invocationDistribution.size() >= metricsProperties.getMaxMetricsCount()
        && !invocationDistribution.containsKey(buildName(id, stage, statusCode))) {
      LOGGER.warn("metrics count size exceed count {}",
          metricsProperties.getMaxMetricsCount());
      return true;
    }
    return false;
  }

  private DistributionSummary getOrCreateInvocationDistribution(String id, String stage, int statusCode) {
    return invocationDistribution.computeIfAbsent(buildName(id, stage, statusCode),
        key -> DistributionSummary.builder(METRICS_CALLS)
            .description("invocation distribution")
            .tag(TAG_NAME, id)
            .tag(TAG_STAGE, stage)
            .tag(TAG_STATUS, Integer.toString(statusCode))
            .distributionStatisticExpiry(metricsProperties.getDistributionStatisticExpiry())
            .serviceLevelObjectives(toDoubleArray(metricsProperties.getServiceLevelObjectives()))
            .register(meterRegistry));
  }

  private String buildName(String id, String stage, int statusCode) {
    return id + TAGS_SEPARATOR + stage + TAGS_SEPARATOR + statusCode;
  }

  private double[] toDoubleArray(List<Integer> distribution) {
    double[] result = new double[distribution.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = ((Long) TimeUnit.MILLISECONDS.toNanos(distribution.get(i))).doubleValue();
    }
    return result;
  }
}
