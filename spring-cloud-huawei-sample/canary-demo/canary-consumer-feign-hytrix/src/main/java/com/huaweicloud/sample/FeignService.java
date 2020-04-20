package com.huaweicloud.sample;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author GuoYl123
 * @Date 2019/10/22
 **/
@FeignClient(name = "canary-provider")
public interface FeignService {
    @GetMapping("/provider")
    String sayHello(@RequestParam("id") Long id);
}
