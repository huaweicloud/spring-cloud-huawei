package com.huaweicloud.sample;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author GuoYl123
 * @Date 2019/12/18
 **/
@RestController
@RequestMapping("/provider2")
public class ProviderController2 {

  @PostMapping("/hello2")
  public String sayHello2() {
    return "hello world2";
  }
}
