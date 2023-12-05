package com.huaweicloud.sample.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@FeignClient(value = "discovery-gateway")
public interface ConsumerGatewayFeignService {

  @GetMapping("/sayHelloConsumerGateway")
  String sayHelloConsumerGateway(@RequestParam("name") String name);
}
