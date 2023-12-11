package com.huaweicloud.governance.adapters;

import org.springframework.core.env.Environment;

public class GovernanceHeaderStatusUtils {
  private static final String RESPONSE_STATUS_HEADER_DEFAULT_KEY = "X-HTTP-STATUS-CODE";

  public static final String HEADER_KEY = "spring.cloud.servicecomb.governance.response.header.status.key";

  public static String getStatusHeaderKey(Environment environment) {
    return environment.getProperty(HEADER_KEY, String.class, RESPONSE_STATUS_HEADER_DEFAULT_KEY);
  }
}
