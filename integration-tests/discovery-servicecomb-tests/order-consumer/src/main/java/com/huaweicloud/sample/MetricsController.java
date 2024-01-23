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
package com.huaweicloud.sample;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.common.context.InvocationFinishEvent;
import com.huaweicloud.common.context.InvocationStage;
import com.huaweicloud.common.event.EventManager;

@RestController
@RequestMapping("/order/metrics")
public class MetricsController {
  private final RestTemplate restTemplate;

  private final MetricsFeignService feignService;

  private List<InvocationStage> stages = new ArrayList<>();

  private int counter = 0;

  private int metricsCounter = 0;

  @Autowired
  public MetricsController(RestTemplate restTemplate, MetricsFeignService feignService) {
    this.restTemplate = restTemplate;
    this.feignService = feignService;
    EventManager.getEventBoundedAsyncEventBus().register(this);
  }

  @Subscribe
  public void onInvocationFinishEvent(InvocationFinishEvent event) {
    if (event.getInvocationStage().getId().contains("/order/metrics")
        && !event.getInvocationStage().getId().contains("InvocationStage")) {
      stages.add(event.getInvocationStage());
      metricsCounter++;
    }
  }

  // event process should be very fast, so do not check if event processed
  @GetMapping("/testInvocationStage")
  public String testInvocationStage() throws Exception {
    int sleep = 0;
    while (sleep <= 3000 && metricsCounter != counter) {
      Thread.sleep(20);
      sleep = sleep + 20;
    }
    StringBuilder result = new StringBuilder();
    result.append("size=").append(stages.size());
    for (int i = 0; i < stages.size(); i++) {
      result.append("|").append("id=").append(stages.get(i).getId()).append("|");
      List<String> stageKeys = new ArrayList<>();
      stages.get(i).getStages().forEach((k, v) -> stageKeys.add(k));
      stageKeys.stream().sorted().forEach(item -> result.append(item + ":"));
      result.append("|");
    }
    return result.toString();
  }

  @GetMapping("/clearInvocationStage")
  public String clearInvocationStage() {
    stages.clear();
    metricsCounter = 0;
    counter = 0;
    return "success";
  }

  @GetMapping("/testRestTemplate")
  public String testRestTemplate(@RequestParam("name") String name) {
    counter++;
    return restTemplate.getForObject("http://price/price/metrics/testGet?name={1}", String.class, name)
        + restTemplate.postForObject("http://price/price/metrics/testPost", name, String.class);
  }

  @PostMapping("/testFeign")
  public String testFeign(@RequestBody String name) {
    counter++;
    return feignService.testGet(name) + feignService.testPost(name);
  }

  @PostMapping("/testFeignAndRestTemplate")
  public String testFeignAndRestTemplate(@RequestBody String name) {
    counter++;
    return restTemplate.getForObject("http://price/price/metrics/testGet?name={1}", String.class, name)
        + restTemplate.getForObject("http://price/price/metrics/testGet?name={1}", String.class, name)
        + feignService.testPost(name) + feignService.testPost(name);
  }
}
