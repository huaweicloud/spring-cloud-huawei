package com.huaweicloud.sample;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class SwaggerConfig {

  @Bean
  public Docket customDocket() {
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(apiInfo());
  }

  private ApiInfo apiInfo() {
    // Contact contact = new Contact("小明", "http://www.cnblogs.com/getupmorning/", "zhaoming0018@126.com");
    return new ApiInfoBuilder()
        .title("guo swagger test")
        .description("gyl's test")
        // .contact(contact)
        .version("1.1.0")
        .build();
  }
}
