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
package com.huaweicloud.dtm.consumer.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * @Author wangqijun
 * @Date 12:45 2019-09-18
 **/
@Configuration
public class DtmRestTemplateAutoConfiguration {
  private static final Logger LOGGER = LoggerFactory.getLogger(DtmRestTemplateAutoConfiguration.class);

  @Autowired(required = false)
  private Collection<RestTemplate> restTemplates;

  @Autowired
  private DtmRestTemplateInterceptor dtmRestTemplateInterceptor;

  @Bean
  public DtmRestTemplateInterceptor restTemplateForDtmInterceptor() {
    return new DtmRestTemplateInterceptor();
  }

  @PostConstruct
  public void init() {
    LOGGER.debug("init restTemplate for dtm..");
    if (this.restTemplates != null) {
      for (RestTemplate restTemplate : restTemplates) {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(
            restTemplate.getInterceptors());
        interceptors.add(dtmRestTemplateInterceptor);
        restTemplate.setInterceptors(interceptors);
      }
    }
  }
}
