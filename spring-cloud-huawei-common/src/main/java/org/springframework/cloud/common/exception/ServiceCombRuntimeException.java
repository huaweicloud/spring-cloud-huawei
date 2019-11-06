package org.springframework.cloud.common.exception;

/**
 * @Author GuoYl123
 * @Date 2019/11/4
 **/
public class ServiceCombRuntimeException extends RuntimeException {

  public ServiceCombRuntimeException() {
    super();
  }

  public ServiceCombRuntimeException(String message) {
    super(message);
  }

  public ServiceCombRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
