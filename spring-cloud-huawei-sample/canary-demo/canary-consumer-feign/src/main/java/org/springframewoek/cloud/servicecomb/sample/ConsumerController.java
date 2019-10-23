package org.springframewoek.cloud.servicecomb.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author GuoYl123
 * @Date 2019/10/22
 **/
@RestController
public class ConsumerController {

    @Autowired
    private FeignService feignService;

    @RequestMapping("/canary")
    public String sayHello(@RequestParam("id") long id) {
        return feignService.sayHello(id);
    }
}
