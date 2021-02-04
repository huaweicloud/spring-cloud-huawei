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

import com.huaweicloud.common.event.ConfigRefreshEvent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
public class PriceController implements ApplicationListener<ConfigRefreshEvent> {

  @Value("${server.port}")
  private Integer port;

  @Value("${dd:}")
  private String dd;

  /**
   * 服务接口
   * @param id
   * @return
   */
  @RequestMapping("/price")
  public String sayHello(@RequestParam("id") String id) {

    return "price ---> " + id + " port -->" + dd;
  }

  public void onApplicationEvent(ConfigRefreshEvent event) {
    System.out.println("change = [" + event.getChange() + "]");
  }
}
