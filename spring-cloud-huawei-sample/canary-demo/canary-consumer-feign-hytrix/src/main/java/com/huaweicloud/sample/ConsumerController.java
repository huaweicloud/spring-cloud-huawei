package com.huaweicloud.sample;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author GuoYl123
 * @Date 2019/10/22
 **/
@EnableCircuitBreaker
@RestController
public class ConsumerController {

    @Autowired
    private FeignService feignService;

    @HystrixCommand(fallbackMethod = "serviceFallback")
    @RequestMapping("/canary")
    public String sayHello(@RequestParam("id") long id, @RequestParam(value = "fail", defaultValue = "false") boolean fail) {
        if (fail) {
            throw new RuntimeException("fail");
        }
        return feignService.sayHello(id);
    }

    public String serviceFallback(long id, boolean fail) {
        return id + "  error";
    }
}
