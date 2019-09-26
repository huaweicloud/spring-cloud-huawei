package org.springframework.cloud.dtm.consumer.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

/**
 * @Author wangqijun
 * @Date 10:07 2019-09-26
 **/
@Configuration
public class DtmFeignConfiguration {

  @Bean
  public RequestInterceptor requestInterceptor() {
    return new DtmRequestInterceptor();
  }
}
