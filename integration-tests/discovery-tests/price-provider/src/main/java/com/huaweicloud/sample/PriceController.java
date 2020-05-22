package com.huaweicloud.sample;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PriceController {

  @RequestMapping("/price")
  public String sayHello(@RequestParam("id") String id) {
    return id;
  }
}
