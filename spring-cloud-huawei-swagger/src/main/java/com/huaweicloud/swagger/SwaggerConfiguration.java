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
package com.huaweicloud.swagger;

import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Author GuoYl123
 * @Date 2019/12/17
 **/
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

  @Bean
  @Lazy
  public ServiceCombSwaggerHandler swaggerHandler() {
    return new ServiceCombSwaggerHandlerImpl();
  }

  @Bean
  public ApiModelReaderAop apiModelReaderAop() {
    return new ApiModelReaderAop();
  }
}
