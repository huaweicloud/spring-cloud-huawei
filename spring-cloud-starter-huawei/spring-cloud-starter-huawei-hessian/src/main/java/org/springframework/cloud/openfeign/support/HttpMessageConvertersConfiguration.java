/*
 * Copyright 2013-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.openfeign.support;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

@Configuration
public class HttpMessageConvertersConfiguration {
  @Bean
  @ConditionalOnMissingBean(name = "clientHttpMessageConvertersCustomizer")
  public ClientHttpMessageConvertersCustomizer hessianClientHttpMessageConvertersCustomizer(
      ObjectProvider<List<HttpMessageConverter<?>>> convertersProvider) {
    return new HessianClientHttpMessageConvertersCustomizer(convertersProvider.getIfAvailable());
  }

  @Bean
  @ConditionalOnMissingBean(name = "feignHttpMessageConverters")
  public FeignHttpMessageConverters feignHttpMessageConverters(
      ObjectProvider<List<HttpMessageConverter<?>>> converters,
      ObjectProvider<ClientHttpMessageConvertersCustomizer> clientCustomizers,
      ObjectProvider<HttpMessageConverterCustomizer> converterCustomizers) {
    List<HttpMessageConverterCustomizer> customizers = converterCustomizers.orderedStream().toList();
    for (HttpMessageConverterCustomizer customizer : customizers) {
      customizer.accept(converters.getIfAvailable());
    }
    return new FeignHttpMessageConverters(clientCustomizers, converterCustomizers);
  }
}
