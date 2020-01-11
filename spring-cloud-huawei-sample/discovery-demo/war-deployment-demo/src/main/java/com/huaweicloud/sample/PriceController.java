package com.huaweicloud.sample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PriceController {

  @Value("${server.port}")
  private Integer port;

  /**
   * 服务接口
   * @param id
   * @return
   */
  @RequestMapping("/price")
  public String sayHello(@RequestParam("id") String id) {
    return "price ---> " + id + " port -->" + port;
  }
}
