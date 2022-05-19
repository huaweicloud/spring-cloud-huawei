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

package com.huaweicloud.common.adapters.webmvc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
public class WebmvcConfiguration implements WebMvcConfigurer {
  private List<PreHandlerInterceptor> preHandlerInterceptors;

  private List<PostHandlerInterceptor> postHandlerInterceptors;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new DecorateHandlerInterceptor(preHandlerInterceptors, postHandlerInterceptors))
        .addPathPatterns("/**");
  }

  @Autowired(required = false)
  public void setPreHandlerInterceptors(List<PreHandlerInterceptor> preHandlerInterceptors) {
    this.preHandlerInterceptors = preHandlerInterceptors;
  }

  @Autowired(required = false)
  public void setPostHandlerInterceptors(List<PostHandlerInterceptor> postHandlerInterceptors) {
    this.postHandlerInterceptors = postHandlerInterceptors;
  }

  @Bean
  public PreHandlerInterceptor deserializeContextPreHandlerInterceptor() {
    return new DeserializeContextPreHandlerInterceptor();
  }
}
