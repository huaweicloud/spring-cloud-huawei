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
package com.huaweicloud.sample.hessian;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration
public class DateFormatConfiguration {
  private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ssZ";

  @Autowired
  RequestMappingHandlerAdapter handlerAdapter;

  @PostConstruct
  public void initConverter() {
    ConfigurableWebBindingInitializer initializer = (ConfigurableWebBindingInitializer) handlerAdapter.getWebBindingInitializer();
    if (initializer.getConversionService() instanceof GenericConversionService) {
      GenericConversionService conversionService = (GenericConversionService) initializer.getConversionService();
      conversionService.addConverter(String.class, Date.class, new StringToDateConverter());
    }
  }

  @Bean
  public FeignFormatterRegistrar feignFormatterRegistrar() {
    return formatterRegistry -> formatterRegistry.addConverter(Date.class, String.class, new DateToStringConverter());
  }

  public static class DateToStringConverter implements Converter<Date, String> {
    @Override
    public String convert(Date source) {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.CHINA);
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
      return simpleDateFormat.format(source);
    }
  }

  public static class StringToDateConverter implements Converter<String, Date> {
    @Override
    public Date convert(String source) {
      try {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.CHINA);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return simpleDateFormat.parse(source);
      } catch (ParseException e) {
        return null;
      }
    }
  }
}
