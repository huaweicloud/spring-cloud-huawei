package com.huaweicloud.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

  @Autowired
  private FeignService feignService;

  @RequestMapping("/product")
  public String getProduct(@RequestParam("id") long id) {
    return feignService.getPrice(id);
  }
}
