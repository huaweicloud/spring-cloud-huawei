package com.huaweicloud.sample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author GuoYl123
 * @Date 2019/12/16
 **/
@RestController
public class ProviderController {

  @GetMapping("/foo")
  public Foo foo(@RequestParam("id") int id) {
    return new Foo("foo", id, null);
  }

  @GetMapping("/hello")
  public String sayHello() {
    return "\"spring cloud hello world\"";
  }
}
