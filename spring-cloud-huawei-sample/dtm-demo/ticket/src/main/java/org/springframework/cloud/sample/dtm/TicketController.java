package org.springframework.cloud.sample.dtm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping(value = "/bookTicket")
  @DTMTccBranch(identifier = "ticket", confirmMethod = "confirm", cancelMethod = "cancel")
  public void bookTicket() throws InterruptedException {
    LOGGER.info("book ticket");
  }

  public void confirm() {
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
