/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.sample;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

public class GovernanceControllerIT {
  String url = "http://127.0.0.1:8088";

  RestTemplate template = new RestTemplate();

  @Test
  public void testRetry() {
    String invocationID = UUID.randomUUID().toString();
    String result = template.getForObject(url + "/govern/retry?invocationID={1}", String.class, invocationID);
    assertThat(result).isEqualTo("try times: 3");
  }

  //TODO: 测试用例无法通过，应该需要通过用例
//  @Test
//  public void testRateLimiting() throws Exception {
//    CountDownLatch latch = new CountDownLatch(200);
//    AtomicBoolean failed = new AtomicBoolean(false);
//
//    for (int i = 0; i < 20; i++) {
//      for (int j = 0; j < 10; j++) {
//        new Thread() {
//          public void run() {
//            try {
//              String result = template.getForObject(url + "/govern/hello", String.class);
//              Assert.assertEquals(result, "Hello world!");
//            } catch (Exception e) {
//              Assert.assertEquals("", e.getMessage());
//              failed.set(true);
//              e.printStackTrace();
//            }
//            latch.countDown();
//          }
//        }.start();
//      }
//      Thread.sleep(100);
//    }
//
//    latch.await(20, TimeUnit.SECONDS);
//    Assert.assertEquals(true, failed.get());
//  }
}
