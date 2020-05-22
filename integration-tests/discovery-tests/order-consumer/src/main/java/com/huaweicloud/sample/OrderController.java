package com.huaweicloud.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class OrderController {

  @Autowired
  private DiscoveryClient discoveryClient;

  @Autowired
  private RestTemplate restTemplate;

  @RequestMapping("/instances")
  public Object instances() {
    return discoveryClient.getInstances("price");
  }

  @RequestMapping("/order")
  public String getOrder(@RequestParam("id") String id) {
    String callServiceResult = restTemplate.getForObject("http://price/price?id=" + id, String.class);
    return callServiceResult;
  }

  @RequestMapping(value = "/services", method = RequestMethod.GET)
  public Object services() {
    return discoveryClient.getServices();
  }
}
