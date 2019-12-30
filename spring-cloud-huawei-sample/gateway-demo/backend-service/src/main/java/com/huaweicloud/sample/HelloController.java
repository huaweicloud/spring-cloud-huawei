package com.huaweicloud.sample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author GuoYl123
 * @Date 2019/10/22
 **/
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String sayHello() {
    return "hello";
  }
}
