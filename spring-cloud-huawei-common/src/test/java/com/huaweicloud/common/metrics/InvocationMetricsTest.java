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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class InvocationMetricsTest {
  @Test
  public void testMetricsRecorded() {
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    InvocationMetrics metrics = new InvocationMetrics(meterRegistry);
    metrics.recordFailedCall("/hello", 1, TimeUnit.SECONDS);
    metrics.recordFailedCall("/hello", 1, TimeUnit.SECONDS);
    metrics.recordSuccessfulCall("/hello", 2, TimeUnit.SECONDS);
    metrics.recordSuccessfulCall("/hello", 2, TimeUnit.SECONDS);
    metrics.recordSuccessfulCall("/hello", 1, TimeUnit.SECONDS);

    List<Meter> meters = meterRegistry.getMeters();
    Assertions.assertEquals(2, meters.size());
    for (Meter meter : meters) {
      Assertions.assertEquals(InvocationMetrics.METRICS_CALLS, meter.getId().getName());
      Assertions.assertEquals("/hello", meter.getId().getTag(InvocationMetrics.TAG_NAME));

      Timer timer = (Timer) meter;

      if (meter.getId().getTag(InvocationMetrics.TAG_KIND).equals(InvocationMetrics.CALLS_TAG_SUCCESSFUL)) {
        Assertions.assertEquals(3, timer.count());
        Assertions.assertEquals(5, ((Double) timer.totalTime(TimeUnit.SECONDS)).intValue());
      } else if (meter.getId().getTag(InvocationMetrics.TAG_KIND).equals(InvocationMetrics.CALLS_TAG_FAILED)) {
        Assertions.assertEquals(2, timer.count());
        Assertions.assertEquals(2, ((Double) timer.totalTime(TimeUnit.SECONDS)).intValue());
      } else {
        Assertions.fail();
      }
    }
  }
}
