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
package com.huaweicloud.router.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.huaweicloud.router.client.hytrix.RouterHystrixConcurrencyStrategy;
import com.huaweicloud.router.client.rest.RouterRestTemplateIntercptor;
import com.huaweicloud.router.client.track.RouterHandlerInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
@Configuration
public class RouterWebMvcConfigurer extends WebMvcConfigurerAdapter {

  @Autowired
  RouterHandlerInterceptor routerHandlerInterceptor;

  @Bean
  public RouterRestTemplateIntercptor routerClientHttpRequestIntercptor(
      @Autowired(required = false) @LoadBalanced List<RestTemplate> restTemplates) {
    RouterRestTemplateIntercptor intercptor = new RouterRestTemplateIntercptor();
    if (restTemplates != null) {
      restTemplates.forEach(restTemplate -> restTemplate.getInterceptors().add(intercptor));
    }
    return intercptor;
  }

  @Bean
  public RouterHystrixConcurrencyStrategy routerHystrixConcurrencyStrategy() {
    return new RouterHystrixConcurrencyStrategy();
  }
  
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(routerHandlerInterceptor).addPathPatterns("/**");
  }
}
