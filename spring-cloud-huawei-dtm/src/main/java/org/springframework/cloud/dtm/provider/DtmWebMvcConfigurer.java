package org.springframework.cloud.dtm.provider;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author wangqijun
 * @Date 18:34 2019-09-25
 **/
@Configuration
public class DtmWebMvcConfigurer implements WebMvcConfigurer {
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new DtmHandlerInterceptor()).addPathPatterns("/**");
  }
}
