package com.huaweicloud.sample;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @Author GuoYl123
 * @Date 2020/4/22
 **/
@RestController
public class ConsumerController {

  @Autowired
  private RestTemplate restTemplate;

  private int count = 0;

  @RequestMapping("/hello")
  public String hello() {
    return restTemplate.getForObject("http://provider/hello", String.class);
  }

  @RequestMapping("/retry")
  public String retry() {
    return restTemplate.getForObject("http://provider/retry", String.class);
  }

  @RequestMapping("/circuitBreaker")
  public String circuitBreaker() throws Exception {
    count++;
    if (count % 3 == 0) {
      return "ok";
    }
    throw new RuntimeException("test error");
  }

  @RequestMapping("/bulkhead")
  public String bulkhead() {
    return restTemplate.getForObject("http://provider/hello", String.class);
  }
}
