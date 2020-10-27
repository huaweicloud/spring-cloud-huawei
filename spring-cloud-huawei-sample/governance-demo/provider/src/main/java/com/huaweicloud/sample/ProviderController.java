package com.huaweicloud.sample;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author GuoYl123
 * @Date 2020/4/22
 **/
@RestController
public class ProviderController {

  private int retryTimes = 0;

  @RequestMapping("/hello")
  public String sayHello() {
    return "Hello world!";
  }

  @RequestMapping("/retry")
  public String retry(HttpServletResponse response) {
    retryTimes++;
    if (retryTimes % 3 == 0) {
      return "try times:" + retryTimes;
    }
    response.setStatus(502);
    return null;
  }
}
