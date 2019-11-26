package com.huaweicloud.sample;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author wangqijun
 * @Date 15:59 2019-07-11
 **/

@FeignClient(serviceId = "price")
public interface FeignService {
  @PostMapping("/price")
  String getPrice(@RequestParam("id") Long id);
}
