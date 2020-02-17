package com.huaweicloud.common.exception;

import org.junit.Test;

/**
 * @Author GuoYl123
 * @Date 2020/2/6
 **/
public class ExceptionTest {

  @Test
  public void exceptionTest() {
    try {
      throw new RemoteOperationException("xx");
    } catch (Exception e) {
    }
    try {
      throw new RemoteServerUnavailableException("xx");
    } catch (Exception e) {
    }
    try {
      throw new RouterIllegalParamException("xx");
    } catch (Exception e) {
    }
    try {
      throw new ServiceCombException("xx");
    } catch (Exception e) {
    }
    try {
      throw new ServiceCombRuntimeException("xx");
    } catch (Exception e) {
    }
  }
}
