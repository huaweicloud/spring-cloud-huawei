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

package com.huaweicloud.common.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.common.configration.dynamic.MetricsProperties;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * Invocation metrics maintains metrics for `the execution of an operation` of a microservice.
 *
 * e.g. when the microservice receive a request for path `/foo/bar`, invocation metrics
 * record the successful calls, failed calls and time taken for this path.
 */
public class InvocationMetrics {
  private static final Logger LOGGER = LoggerFactory.getLogger(InvocationMetrics.class);

  public static final String CONTEXT_TIME = "x-m-start";

  public static final String CONTEXT_OPERATION = "x-m-operation";

  public static final String METRICS_PREFIX = "metrics.invocation.";

  public static final String TAG_KIND = "kind";

  public static final String TAG_NAME = "name";

  // calls: Timer
  public static final String METRICS_CALLS = METRICS_PREFIX + "calls";

  public static final String CALLS_TAG_FAILED = "failed";

  public static final String CALLS_TAG_SUCCESSFUL = "successful";

  private final MeterRegistry meterRegistry;

  private final MetricsProperties metricsProperties;

  private final Pattern includePatter;

  private final Pattern excludePatter;

  private final ConcurrentMap<String, Timer> successfulCalls = new ConcurrentHashMap<>();

  private final ConcurrentMap<String, Timer> failedCalls = new ConcurrentHashMap<>();

  public InvocationMetrics(MeterRegistry meterRegistry, MetricsProperties metricsProperties) {
    this.meterRegistry = meterRegistry;
    this.metricsProperties = metricsProperties;
    if (StringUtils.isNotEmpty(metricsProperties.getExcludePattern())) {
      excludePatter = Pattern.compile(metricsProperties.getIncludePattern());
    } else {
      excludePatter = null;
    }
    if (StringUtils.isNotEmpty(metricsProperties.getIncludePattern())) {
      includePatter = Pattern.compile(metricsProperties.getIncludePattern());
    } else {
      includePatter = null;
    }
  }

  public void recordSuccessfulCall(String name, long amount, TimeUnit timeUnit) {
    if (byPassMethod(name, successfulCalls)) {
      return;
    }
    Timer timer = getOrCreateSuccessfulCalls("Total number of successful calls", name);
    timer.record(amount, timeUnit);
  }

  private boolean byPassMethod(String name, Map<String, Timer> group) {
    if (excludePatter != null && excludePatter.matcher(name).matches()) {
      return true;
    }
    if (includePatter != null && !includePatter.matcher(name).matches()) {
      return true;
    }
    if (group.size() >= metricsProperties.getMaxMethodCount()
        && !group.containsKey(name)) {
      LOGGER.warn("metrics method exceed count {}", metricsProperties.getMaxMethodCount());
      return true;
    }
    return false;
  }

  public void recordFailedCall(String name, long amount, TimeUnit timeUnit) {
    if (byPassMethod(name, failedCalls)) {
      return;
    }
    Timer timer = getOrCreateFailedCalls("Total number of failed calls", name);
    timer.record(amount, timeUnit);
  }

  private Timer getOrCreateSuccessfulCalls(String description, String name) {
    return successfulCalls.computeIfAbsent(name, key -> Timer.builder(METRICS_CALLS)
        .description(description)
        .tag(TAG_NAME, key)
        .tag(TAG_KIND, CALLS_TAG_SUCCESSFUL)
        .register(meterRegistry));
  }

  private Timer getOrCreateFailedCalls(String description, String name) {
    return failedCalls.computeIfAbsent(name, key -> Timer.builder(METRICS_CALLS)
        .description(description)
        .tag(TAG_NAME, key)
        .tag(TAG_KIND, CALLS_TAG_FAILED)
        .register(meterRegistry));
  }
}
