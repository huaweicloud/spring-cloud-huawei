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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.huaweicloud.common.configration.dynamic.MetricsProperties;
import com.huaweicloud.common.context.InvocationStage;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class InvocationMetricsTest {
  @Test
  public void testMetricsRecorded() {
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    MetricsProperties metricsProperties = new MetricsProperties();
    InvocationMetrics metrics = new InvocationMetrics(meterRegistry, metricsProperties);
    metrics.recordInvocationDistribution("/hello", InvocationStage.STAGE_ALL, 500, TimeUnit.MILLISECONDS.toNanos(1));
    metrics.recordInvocationDistribution("/hello", InvocationStage.STAGE_ALL, 500, TimeUnit.MILLISECONDS.toNanos(1));
    metrics.recordInvocationDistribution("/hello", InvocationStage.STAGE_ALL, 200, TimeUnit.MILLISECONDS.toNanos(2));
    metrics.recordInvocationDistribution("/hello", InvocationStage.STAGE_ALL, 200, TimeUnit.MILLISECONDS.toNanos(2));
    metrics.recordInvocationDistribution("/hello", InvocationStage.STAGE_ALL, 200, TimeUnit.MILLISECONDS.toNanos(1));

    List<Meter> meters = meterRegistry.getMeters();
    AtomicInteger count = new AtomicInteger(0);
    for (Meter meter : meters) {
      if (!InvocationMetrics.METRICS_CALLS.equals(meter.getId().getName())) {
        continue;
      }
      count.incrementAndGet();
      Assertions.assertEquals("/hello", meter.getId().getTag(InvocationMetrics.TAG_NAME));
      DistributionSummary summary = (DistributionSummary) meter;
      if (Integer.parseInt(meter.getId().getTag(InvocationMetrics.TAG_STATUS)) == 200) {
        Assertions.assertEquals(3, summary.count());
        Assertions.assertEquals(5, TimeUnit.NANOSECONDS.toMillis(Double.valueOf(summary.totalAmount()).longValue()));
      } else if (Integer.parseInt(meter.getId().getTag(InvocationMetrics.TAG_STATUS)) == 500) {
        Assertions.assertEquals(2, summary.count());
        Assertions.assertEquals(2, TimeUnit.NANOSECONDS.toMillis(Double.valueOf(summary.totalAmount()).longValue()));
      } else {
        Assertions.fail();
      }
    }
    Assertions.assertEquals(2, count.get());
  }

  @Test
  public void testMetricsIncludePattern() {
    for (int i = 0; i < 5; i++) {
      testMetricsIncludePatternImpl();
    }
  }

  private void testMetricsIncludePatternImpl() {
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    MetricsProperties metricsProperties = new MetricsProperties();
    metricsProperties.setIncludePattern("/hello.*");
    metricsProperties.setMaxMetricsCount(4);
    InvocationMetrics metrics = new InvocationMetrics(meterRegistry, metricsProperties);
    metrics.recordInvocationDistribution("/hello", InvocationStage.STAGE_ALL, 500, TimeUnit.MILLISECONDS.toNanos(1));
    metrics.recordInvocationDistribution("/hello", InvocationStage.STAGE_ALL, 500, TimeUnit.MILLISECONDS.toNanos(1));
    metrics.recordInvocationDistribution("/hello", InvocationStage.STAGE_ALL, 200, TimeUnit.MILLISECONDS.toNanos(2));
    metrics.recordInvocationDistribution("/hello", InvocationStage.STAGE_ALL, 200, TimeUnit.MILLISECONDS.toNanos(2));
    metrics.recordInvocationDistribution("/hello", InvocationStage.STAGE_ALL, 200, TimeUnit.MILLISECONDS.toNanos(1));
    metrics.recordInvocationDistribution("/wrong", InvocationStage.STAGE_ALL, 200, TimeUnit.MILLISECONDS.toNanos(1));
    metrics.recordInvocationDistribution("/wrong", InvocationStage.STAGE_ALL, 500, TimeUnit.MILLISECONDS.toNanos(1));
    metrics.recordInvocationDistribution("/hello/other", InvocationStage.STAGE_ALL, 200,
        TimeUnit.MILLISECONDS.toNanos(2));
    metrics.recordInvocationDistribution("/hello/other", InvocationStage.STAGE_ALL, 500,
        TimeUnit.MILLISECONDS.toNanos(1));
    metrics.recordInvocationDistribution("/hello/exceed", InvocationStage.STAGE_ALL, 200,
        TimeUnit.MILLISECONDS.toNanos(1));
    metrics.recordInvocationDistribution("/hello/exceed", InvocationStage.STAGE_ALL, 500,
        TimeUnit.MILLISECONDS.toNanos(1));

    List<Meter> meters = meterRegistry.getMeters();
    int count = 0;
    for (Meter meter : meters) {
      if (!InvocationMetrics.METRICS_CALLS.equals(meter.getId().getName())) {
        continue;
      }
      count++;

      DistributionSummary summary = (DistributionSummary) meter;

      if (Integer.parseInt(meter.getId().getTag(InvocationMetrics.TAG_STATUS)) == 200
          && "/hello".equals(meter.getId().getTag(InvocationMetrics.TAG_NAME))) {
        Assertions.assertEquals(3, summary.count());
        Assertions.assertEquals(5, TimeUnit.NANOSECONDS.toMillis(Double.valueOf(summary.totalAmount()).longValue()));
      } else if (Integer.parseInt(meter.getId().getTag(InvocationMetrics.TAG_STATUS)) == 500
          && "/hello".equals(meter.getId().getTag(InvocationMetrics.TAG_NAME))) {
        Assertions.assertEquals(2, summary.count());
        Assertions.assertEquals(2, TimeUnit.NANOSECONDS.toMillis(Double.valueOf(summary.totalAmount()).longValue()));
      } else if (Integer.parseInt(meter.getId().getTag(InvocationMetrics.TAG_STATUS)) == 200
          && "/hello/other".equals(meter.getId().getTag(InvocationMetrics.TAG_NAME))) {
        Assertions.assertEquals(1, summary.count());
        Assertions.assertEquals(2, TimeUnit.NANOSECONDS.toMillis(Double.valueOf(summary.totalAmount()).longValue()));
      } else if (Integer.parseInt(meter.getId().getTag(InvocationMetrics.TAG_STATUS)) == 500
          && "/hello/other".equals(meter.getId().getTag(InvocationMetrics.TAG_NAME))) {
        Assertions.assertEquals(1, summary.count());
        Assertions.assertEquals(1, TimeUnit.NANOSECONDS.toMillis(Double.valueOf(summary.totalAmount()).longValue()));
      } else {
        Assertions.fail();
      }
    }

    Assertions.assertEquals(4, count);
  }
}
