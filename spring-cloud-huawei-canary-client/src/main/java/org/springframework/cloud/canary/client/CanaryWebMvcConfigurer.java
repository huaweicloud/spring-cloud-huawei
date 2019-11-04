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
package org.springframework.cloud.canary.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.canary.client.hytrix.CanaryHystrixConcurrencyStrategy;
import org.springframework.cloud.canary.client.rest.CanaryRestTemplateIntercptor;
import org.springframework.cloud.canary.client.track.CanaryHandlerInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
@Configuration
public class CanaryWebMvcConfigurer implements WebMvcConfigurer {

    @Bean
    public CanaryRestTemplateIntercptor canaryClientHttpRequestIntercptor(
        @Autowired(required = false) @LoadBalanced List<RestTemplate> restTemplates) {
        CanaryRestTemplateIntercptor intercptor = new CanaryRestTemplateIntercptor();
        if (restTemplates != null) {
            restTemplates.forEach(restTemplate -> restTemplate.getInterceptors().add(intercptor));
        }
        return intercptor;
    }

    @Bean
    public CanaryHystrixConcurrencyStrategy canaryHystrixConcurrencyStrategy() {
        return new CanaryHystrixConcurrencyStrategy();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CanaryHandlerInterceptor()).addPathPatterns("/**");
    }
}
