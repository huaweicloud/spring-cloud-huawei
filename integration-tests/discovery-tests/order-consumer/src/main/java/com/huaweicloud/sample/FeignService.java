package com.huaweicloud.sample;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "price")
public interface FeignService {
  @PostMapping("/price")
  String getPrice(@RequestParam("id") Long id);

  @RequestMapping("/retry")
  String retry(@RequestParam(name = "invocationID") String invocationID);
}
