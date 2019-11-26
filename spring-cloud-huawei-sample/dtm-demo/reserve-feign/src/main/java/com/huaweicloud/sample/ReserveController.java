package com.huaweicloud.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huawei.middleware.dtm.client.annotations.DTMTxBegin;

/**
 * @Author wangqijun
 * @Date 17:09 2019-09-10
 **/
@RestController
public class ReserveController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReserveController.class);

  @Autowired
  private CouponService couponService;

  @Autowired
  private TicketService ticketService;

  @RequestMapping("/reserve")
  @DTMTxBegin(appName = "reserve-feign")
  public String getOrder(@RequestParam("id") String id) {
    String discountCouponResult = couponService.discountCoupon(id);
    LOGGER.info("bookRoomResult:" + discountCouponResult);
    String bookTicketResult = ticketService.bookTicket(id);
    LOGGER.info("bookTicketResult:" + bookTicketResult);
    return discountCouponResult + "-------" + bookTicketResult;
  }
}
