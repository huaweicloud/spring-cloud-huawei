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

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author GuoYl123
 * @Date 2019/10/22
 **/
@EnableCircuitBreaker
@RestController
public class ConsumerController {

    @Autowired
    private FeignService feignService;

    @HystrixCommand(fallbackMethod = "serviceFallback")
    @RequestMapping("/canary")
    public String sayHello(@RequestParam("id") long id, @RequestParam(value = "fail", defaultValue = "false") boolean fail) {
        if (fail) {
            throw new RuntimeException("fail");
        }
        return feignService.sayHello(id);
    }

    public String serviceFallback(long id, boolean fail) {
        return id + "  error";
    }
}
