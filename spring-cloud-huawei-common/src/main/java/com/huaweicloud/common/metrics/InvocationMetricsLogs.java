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
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.common.configration.dynamic.MetricsProperties;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.distribution.CountAtBucket;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.netty.util.concurrent.DefaultThreadFactory;

public class InvocationMetricsLogs {
  private static final Logger LOGGER = LoggerFactory.getLogger("metrics_logger");

  private static final int CORE_SIZE = 1;

  private final MeterRegistry meterRegistry;

  private final MetricsProperties metricsProperties;

  static class MetricsData {
    long lastCount = 0;

    long cycleCount = 0;

    double lastAmount = 0D;

    double cycleAmount = 0D;

    double[] lastDistribution;

    double[] cycleDistribution;
  }

  private long lastTime = -1;

  private final Object LOCK = new Object();

  private final Map<String, Map<String, Map<String, MetricsData>>> LOGS_MODEL = new TreeMap<>();

  public InvocationMetricsLogs(MeterRegistry meterRegistry,
      MetricsProperties metricsProperties) {
    this.meterRegistry = meterRegistry;
    this.metricsProperties = metricsProperties;

    ThreadFactory threadFactory = new DefaultThreadFactory("metrics", true);
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(CORE_SIZE, threadFactory);
    executorService.scheduleWithFixedDelay(this::pollLogs, 5000,
        metricsProperties.getLogsInterval().toMillis(), TimeUnit.MILLISECONDS);
  }

  private void pollLogs() {
    if (metricsProperties.isLogsEnabled()) {
      synchronized (LOCK) {
        pollInvocationMetrics();
        lastTime = System.currentTimeMillis();
      }
    }
  }

  /**
   * [status] [operation] [stage] [requests/cycle/tps] [average] [0, 10) [10,20) [20,50) [50,100) [100,500) [500,2000) [2000,60000)
   * 200:
   *   POST /provider/benchmark/load:
   *     2002/300/6 9 1655 307 39 0 0 1 0  all
   *     2002/300/6 5 1911 81 9 0 0 1 0  gateway
   */
  private void pollInvocationMetrics() {
    List<Meter> meters = this.meterRegistry.getMeters();

    for (Meter meter : meters) {
      if (!InvocationMetrics.METRICS_CALLS.equals(meter.getId().getName())) {
        continue;
      }

      DistributionSummary summary = (DistributionSummary) meter;

      Map<String, Map<String, MetricsData>> statusModel = LOGS_MODEL.computeIfAbsent(
          summary.getId().getTag(InvocationMetrics.TAG_STATUS), k -> new TreeMap<>());
      Map<String, MetricsData> operationModel = statusModel.computeIfAbsent(
          summary.getId().getTag(InvocationMetrics.TAG_NAME), k -> new TreeMap<>());

      MetricsData metricsData = operationModel.computeIfAbsent(summary.getId().getTag(InvocationMetrics.TAG_STAGE),
          k -> new MetricsData());
      metricsData.cycleAmount = summary.totalAmount() - metricsData.lastAmount;
      metricsData.lastAmount = summary.totalAmount();
      metricsData.cycleCount = summary.count() - metricsData.lastCount;
      metricsData.lastCount = summary.count();

      HistogramSnapshot histogramSnapshot = summary.takeSnapshot();
      CountAtBucket[] countAtBuckets = histogramSnapshot.histogramCounts();
      if (metricsData.lastDistribution == null) {
        metricsData.lastDistribution = new double[countAtBuckets.length];
        metricsData.cycleDistribution = new double[countAtBuckets.length];
      }
      for (int i = 0; i < countAtBuckets.length; i++) {
        if (i == 0) {
          metricsData.cycleDistribution[i] = countAtBuckets[i].count() - metricsData.lastDistribution[i];
          metricsData.lastDistribution[i] = countAtBuckets[i].count();
          continue;
        }
        metricsData.cycleDistribution[i] = (countAtBuckets[i].count() - countAtBuckets[i - 1].count())
            - metricsData.lastDistribution[i];
        metricsData.lastDistribution[i] = countAtBuckets[i].count() - countAtBuckets[i - 1].count();
      }
      operationModel.put(summary.getId().getTag(InvocationMetrics.TAG_STAGE), metricsData);
    }

    // ignore the first cycle data
    if (lastTime == -1) {
      return;
    }

    StringBuilder logsBuilder = new StringBuilder();
    logsBuilder.append("[status] [operation] [stage] [requests/cycle/tps] [average] ");
    for (int i = 0; i < metricsProperties.getServiceLevelObjectives().size(); i++) {
      if (i == 0) {
        logsBuilder.append("[0, ").append(metricsProperties.getServiceLevelObjectives().get(i)).append(") ");
        continue;
      }
      logsBuilder.append("[").append(metricsProperties.getServiceLevelObjectives().get(i - 1)).append(",")
          .append(metricsProperties.getServiceLevelObjectives().get(i)).append(") ");
    }
    logsBuilder.append("\n");

    LOGS_MODEL.forEach((statusKey, statusValue) -> {
      logsBuilder.append(statusKey);
      logsBuilder.append(":\n");
      statusValue.forEach((operationKey, operationValue) -> {
        logsBuilder.append("  ");
        logsBuilder.append(operationKey);
        logsBuilder.append(":\n");

        operationValue.forEach((stageKey, stageValue) -> {
          if (stageValue.lastCount == 0) {
            return;
          }
          logsBuilder.append("    ");
          logsBuilder.append(formatMetricsData(stageValue));
          logsBuilder.append(" ");
          logsBuilder.append(stageKey);
          logsBuilder.append("\n");
        });
      });
    });

    LOGGER.info(logsBuilder.toString());
  }

  private String formatMetricsData(MetricsData stageValue) {
    StringBuilder data = new StringBuilder();
    data.append("(").append(stageValue.cycleCount)
        .append("/").append(stageValue.lastCount).append(")").append("/");
    data.append(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastTime)).append("/");
    data.append(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastTime) > 0
        ? stageValue.cycleCount /
        (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastTime)) : 0);
    data.append(" ");
    data.append(stageValue.cycleCount > 0 ? nanosToMillis(stageValue.cycleAmount) / stageValue.cycleCount : 0);
    data.append(" ");
    for (int i = 0; i < stageValue.cycleDistribution.length; i++) {
      data.append("(")
          .append(((Double) stageValue.cycleDistribution[i]).intValue())
          .append("/")
          .append(((Double) stageValue.lastDistribution[i]).intValue())
          .append(")")
          .append(" ");
    }
    return data.toString();
  }

  private long nanosToMillis(Double nanos) {
    return TimeUnit.NANOSECONDS.toMillis(nanos.longValue());
  }
}
