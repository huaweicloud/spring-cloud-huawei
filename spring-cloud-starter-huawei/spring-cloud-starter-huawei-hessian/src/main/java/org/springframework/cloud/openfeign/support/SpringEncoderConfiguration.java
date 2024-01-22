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

package org.springframework.cloud.openfeign.support;

import static feign.form.ContentType.MULTIPART;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import feign.codec.Encoder;
import feign.form.MultipartFormContentProcessor;
import feign.form.spring.SpringFormEncoder;

@Configuration
@ConditionalOnClass(name = {"org.springframework.cloud.openfeign.support.SpringEncoder"})
@SuppressWarnings({"all", "PMD"})
@SuppressFBWarnings
public class SpringEncoderConfiguration {
  @Autowired(required = false)
  private FeignClientProperties feignClientProperties;

  @Autowired(required = false)
  private FeignEncoderProperties encoderProperties;

  @Autowired(required = false)
  private SpringDataWebProperties springDataWebProperties;

  @Autowired
  private ObjectFactory<HttpMessageConverters> messageConverters;

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnMissingClass("org.springframework.data.domain.Pageable")
  public Encoder feignEncoder(ObjectProvider<AbstractFormWriter> formWriterProvider,
      ObjectProvider<HttpMessageConverterCustomizer> customizers) {
    return springEncoder(formWriterProvider, encoderProperties, customizers);
  }

  @Bean
  @ConditionalOnClass(name = "org.springframework.data.domain.Pageable")
  @ConditionalOnMissingBean
  public Encoder feignEncoderPageable(ObjectProvider<AbstractFormWriter> formWriterProvider,
      ObjectProvider<HttpMessageConverterCustomizer> customizers) {
    PageableSpringEncoder encoder = new PageableSpringEncoder(
        springEncoder(formWriterProvider, encoderProperties, customizers));

    if (springDataWebProperties != null) {
      encoder.setPageParameter(springDataWebProperties.getPageable().getPageParameter());
      encoder.setSizeParameter(springDataWebProperties.getPageable().getSizeParameter());
      encoder.setSortParameter(springDataWebProperties.getSort().getSortParameter());
    }
    return encoder;
  }

  private Encoder springEncoder(ObjectProvider<AbstractFormWriter> formWriterProvider,
      FeignEncoderProperties encoderProperties, ObjectProvider<HttpMessageConverterCustomizer> customizers) {
    AbstractFormWriter formWriter = formWriterProvider.getIfAvailable();

    if (formWriter != null) {
      return new ExtendedSpringEncoder(new SpringPojoFormEncoder(formWriter), messageConverters, encoderProperties,
          customizers);
    } else {
      return new ExtendedSpringEncoder(new SpringFormEncoder(), messageConverters, encoderProperties, customizers);
    }
  }

  private class SpringPojoFormEncoder extends SpringFormEncoder {

    SpringPojoFormEncoder(AbstractFormWriter formWriter) {
      super();

      MultipartFormContentProcessor processor = (MultipartFormContentProcessor) getContentProcessor(MULTIPART);
      processor.addFirstWriter(formWriter);
    }
  }
}
