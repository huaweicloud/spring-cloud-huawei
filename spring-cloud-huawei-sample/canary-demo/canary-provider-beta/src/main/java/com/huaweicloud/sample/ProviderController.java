package com.huaweicloud.sample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author GuoYl123
 * @Date 2019/10/22
 **/
@RestController
public class ProviderController {

  @Value("${server.port}")
  private Integer port;

  @RequestMapping("/provider")
  public String sayHello(@RequestParam("id") String id) {
    return "beta: provider ---> " + id + " port -->" + port;
  }
}
