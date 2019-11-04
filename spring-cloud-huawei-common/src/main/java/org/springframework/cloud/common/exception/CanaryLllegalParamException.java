package org.springframework.cloud.common.exception;

/**
 * @Author GuoYl123
 * @Date 2019/11/4
 **/
public class CanaryLllegalParamException extends ServiceCombRuntimeException {

  public CanaryLllegalParamException(String message) {
    super(message);
  }

  public CanaryLllegalParamException(String message, Throwable cause) {
    super(message, cause);
  }
}
