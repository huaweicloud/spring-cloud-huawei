package org.springframework.cloud.dtm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author wangqijun
 * @Date 20:09 2019-09-09
 **/
@Component
@ConfigurationProperties("dtm")
public class DtmProperties {

  @Value("${spring.application.name}")
  private String name;

  @Value("${dtm.proxy.address}")
  private String address;
}
