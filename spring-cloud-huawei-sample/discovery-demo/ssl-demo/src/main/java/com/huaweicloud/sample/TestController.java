package com.huaweicloud.sample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  @Value("${server.port}")
  private Integer port;

  /**
   *
   * @param id
   * @return
   */
  @RequestMapping("/test")
  public String sayHello(@RequestParam("id") String id) {
    return "price ---> " + id + " port -->" + port;
  }
}
