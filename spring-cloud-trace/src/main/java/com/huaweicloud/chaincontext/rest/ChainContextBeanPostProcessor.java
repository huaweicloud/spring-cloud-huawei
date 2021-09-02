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
package com.huaweicloud.chaincontext.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.client.RestTemplate;

public class ChainContextBeanPostProcessor implements BeanPostProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(ChainContextBeanPostProcessor.class);

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof RestTemplate) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("add ChainContextHttpRequestInterceptor to RestTemplate");
      }
      RestTemplate restTemplate = RestTemplate.class.cast(bean);
      restTemplate.getInterceptors().add(new ChainContextHttpRequestInterceptor());
      return restTemplate;
    }
    return bean;
  }
}
