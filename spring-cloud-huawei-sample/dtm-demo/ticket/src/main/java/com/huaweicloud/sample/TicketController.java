package com.huaweicloud.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huawei.middleware.dtm.client.context.DTMContext;
import com.huawei.middleware.dtm.client.tcc.annotations.DTMTccBranch;

/**
 * @Author wangqijun
 * @Date 09:54 2019-09-23
 **/
@RestController
public class TicketController {
  private static final Logger LOGGER = LoggerFactory.getLogger(TicketController.class);

  private String flag;

  @GetMapping(value = "/bookTicket")
  @DTMTccBranch(identifier = "ticket", confirmMethod = "confirm", cancelMethod = "cancel")
  public String bookTicket(@RequestParam("id") String id) throws InterruptedException {
    LOGGER.info("{} - {} try bookTicket");
    flag = id;
    return "bookTicket success";
  }

  public void confirm() {

    if ("sleep".equalsIgnoreCase(flag)) {
      try {
        Thread.sleep(5 * 60 * 1000);
      } catch (InterruptedException e) {
        LOGGER.error(e.getMessage(), e);
      }
    }

    LOGGER.info("{} - {} confirm ticket",
        DTMContext.getDTMContext().getGlobalTxId(),
        DTMContext.getDTMContext().getBranchTxId());
  }

  public void cancel() {
    LOGGER.info("{} - {} cancel ticket",
        DTMContext.getDTMContext().getGlobalTxId(),
        DTMContext.getDTMContext().getBranchTxId());
  }
}
