package com.huaweicloud.sample;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author wangqijun
 * @Date 17:32 2019-10-14
 **/
@FeignClient(name = "ticket")
public interface TicketService {
  @GetMapping("/bookTicket")
  String bookTicket(@RequestParam("id") String id);
}
