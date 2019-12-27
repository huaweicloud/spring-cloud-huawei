package com.huaweicloud.swagger;

import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
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
