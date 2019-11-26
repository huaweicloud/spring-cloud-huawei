package com.huaweicloud.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.huawei.middleware.dtm.client.annotations.DTMTxBegin;

/**
 * @Author wangqijun
 * @Date 17:09 2019-09-10
 **/
@RestController
public class ReserveController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReserveController.class);

  @Autowired
  private DiscoveryClient discoveryClient;

  @Autowired
  private RestTemplate restTemplate;

  @RequestMapping("/reserve")
  @DTMTxBegin(appName = "reserve")
  public String getOrder(@RequestParam("id") String id) {
    String discountCouponResult = restTemplate.getForObject("http://coupon/discountCoupon?id=" + id, String.class);
    LOGGER.info("bookRoomResult:" + discountCouponResult);
    String bookTicketResult = restTemplate.getForObject("http://ticket/bookTicket?id=" + id, String.class);
    LOGGER.info("bookTicketResult:" + bookTicketResult);
    return discountCouponResult + "-------" + bookTicketResult;
  }
}
