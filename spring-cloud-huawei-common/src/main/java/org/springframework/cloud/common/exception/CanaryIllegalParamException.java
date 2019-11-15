package org.springframework.cloud.common.exception;

/**
 * @Author GuoYl123
 * @Date 2019/11/4
 **/
public class CanaryIllegalParamException extends ServiceCombRuntimeException {

  public CanaryIllegalParamException(String message) {
    super(message);
  }

  public CanaryIllegalParamException(String message, Throwable cause) {
    super(message, cause);
  }
}
