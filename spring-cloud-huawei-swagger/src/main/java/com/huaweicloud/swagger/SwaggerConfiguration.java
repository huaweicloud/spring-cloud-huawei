package com.huaweicloud.swagger;

import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @Author GuoYl123
 * @Date 2019/12/17
 **/
@Configuration
public class SwaggerConfiguration {

  @Bean
  @Lazy
  public ServiceCombSwaggerHandler swaggerHandler() {
    return new ServiceCombSwaggerHandlerImpl();
  }
}
