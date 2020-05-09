package com.huaweicloud.common.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author GuoYl123
 * @Date 2020/4/18
 **/
public class BackOff {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackOff.class);

  private static final int MAX_DELAY_TIME = 60 * 1000;

  private int retryDelayTime = 1000;

  public BackOff() {
  }

  public BackOff(int retryDelayTime) {
    this.retryDelayTime = retryDelayTime;
  }

  public void backOff() {
    if (MAX_DELAY_TIME == retryDelayTime) {
      return;
    }
    retryDelayTime *= 2;
    if (MAX_DELAY_TIME <= retryDelayTime) {
      retryDelayTime = MAX_DELAY_TIME;
    }
  }

  public void waiting() {
    try {
      Thread.sleep(getBackOffTime());
    } catch (InterruptedException e) {
      LOGGER.warn("thread interrupted.");
    }
  }

  public void waitingAndBackoff() {
    try {
      Thread.sleep(getBackOffTime());
    } catch (InterruptedException e) {
      LOGGER.warn("thread interrupted.");
    }
    backOff();
  }

  private int getBackOffTime() {
    return retryDelayTime;
  }
}
