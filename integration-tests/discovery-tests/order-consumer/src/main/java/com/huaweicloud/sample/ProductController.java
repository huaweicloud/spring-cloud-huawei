package com.huaweicloud.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ProductController {
  String url = "http://127.0.0.1:8088";

  RestTemplate template = new RestTemplate();

  @Autowired
  private FeignService feignService;

  @RequestMapping("/product")
  public String getProduct(@RequestParam("id") long id) {
    return feignService.getPrice(id);
  }
}
