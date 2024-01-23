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

import java.util.concurrent.CountDownLatch;

import javax.xml.ws.Holder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {
  @Autowired
  private FeignService feignService;

  @RequestMapping("/product")
  public String getProduct(@RequestParam("id") long id) {
    return feignService.getPrice(id);
  }

  @RequestMapping("/productAsync")
  public String getProductAsync(@RequestParam("id") long id) {
    CountDownLatch latch = new CountDownLatch(1);
    Holder<String> holder = new Holder<>();
    new Thread(() -> {
      try {
        holder.value = feignService.getPrice(id);
      } catch (Exception e) {
        e.printStackTrace();
      }
      latch.countDown();
    }).start();
    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return holder.value;
  }
}

