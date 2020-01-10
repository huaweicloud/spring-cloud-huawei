package com.huaweicloud.common.auth;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2020/1/10
 **/
public class AuthHeaderUtils {

  public static Map<String, String> genAuthHeaders() {
    if (Files.exists(Paths.get(AuthHeaderStrategy.DEFAULT_SECRET_AUTH_PATH,
        AuthHeaderStrategy.DEFAULT_SECRET_AUTH_NAME))) {
      AuthHeaderStrategy authHeaderStrategy = new AuthHeaderStrategyMount();
      return authHeaderStrategy.getHeaders();
    }
    return Collections.emptyMap();
  }
}
