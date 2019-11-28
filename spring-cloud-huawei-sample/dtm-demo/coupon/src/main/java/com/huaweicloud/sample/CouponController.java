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
 * @Date 11:41 2019-09-17
 **/
@RestController
public class CouponController {
  private static final Logger LOGGER = LoggerFactory.getLogger(CouponController.class);

  @GetMapping(value = "/discountCoupon")
  @DTMTccBranch(identifier = "coupon", confirmMethod = "confirm", cancelMethod = "cancel")
  public String discountCoupon(@RequestParam("id") String id) throws InterruptedException {
    LOGGER.info("{} - {} try discountCoupon");
    return "  discountCoupon success";
  }

  public void confirm() {
    LOGGER.info("{} - {} confirm coupon",
        DTMContext.getDTMContext().getGlobalTxId(),
        DTMContext.getDTMContext().getBranchTxId());
  }

  public void cancel() {
    LOGGER.info("{} - {} cancel coupon",
        DTMContext.getDTMContext().getGlobalTxId(),
        DTMContext.getDTMContext().getBranchTxId());
  }
}
