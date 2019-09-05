package org.springframework.cloud.servicecomb.discovery.client;

import java.util.Map;

import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicecomb.discovery.client.model.SSLConfig;

import com.huawei.paas.foundation.auth.AuthHeaderUtils;
import com.huawei.paas.foundation.auth.signer.utils.SignerUtils;

/**
 * @Author wangqijun
 * @Date 18:44 2019-09-05
 **/
public class DealHeaderUtil {

  public static final int CONNECT_TIMEOUT = 5000;

  public static final int CONNECTION_REQUEST_TIMEOUT = 5000;

  public static final int SOCKET_TIMEOUT = 5000;

  public static final int MAX_TOTAL = 1000;

  public static final int DEFAULT_MAX_PER_ROUTE = 500;

  public static final String X_DOMAIN_NAME = "x-domain-name";

  public static final String DEFAULT_X_DOMAIN_NAME = "default";

  public static final String CONTENT_TYPE = "Content-type";

  public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

  public static final String X_SERVICE_AK = "X-Service-AK";

  public static final String X_SERVICE_SHA_AKSK = "X-Service-ShaAKSK";

  public static final String X_SERVICE_PROJECT = "X-Service-Project";

  private static final Logger LOGGER = LoggerFactory.getLogger(DealHeaderUtil.class);

  public static void addAKSKHeader(HttpUriRequest httpRequest,
      SSLConfig sslConfig) {
    AuthHeaderUtils authHeaderUtils = AuthHeaderUtils.getInstance();
    Map<String, String> headerMap = authHeaderUtils.genAuthHeaders();
    for (Map.Entry<String, String> entry : headerMap.entrySet()) {
      httpRequest.addHeader(entry.getKey(), entry.getValue());
    }
    if (isHeaderMapEmpty(headerMap) && isSSLConfigNotEmpty(sslConfig)) {
      httpRequest.addHeader(X_SERVICE_AK, sslConfig.getAccessKey());
      httpRequest.addHeader(X_SERVICE_SHA_AKSK,
          encode(sslConfig));
      httpRequest.addHeader(X_SERVICE_PROJECT, sslConfig.getProject());
    }
  }

  public static boolean isHeaderMapEmpty(Map<String, String> headerMap) {
    return headerMap == null || headerMap.size() == 0;
  }

  public static boolean isSSLConfigNotEmpty(SSLConfig sslConfig) {
    return sslConfig.getAccessKey() != null && sslConfig.getSecretKey() != null && sslConfig.getProject() != null;
  }

  private static String encode(SSLConfig sslConfig) {

    if ("ShaAKSKCipher".equalsIgnoreCase(sslConfig.getAkskCustomCipher())) {
      return sslConfig.getSecretKey();
    }
    try {
      return SignerUtils.sha256Encode(sslConfig.getSecretKey(), sslConfig.getAccessKey());
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return null;
    }
  }

  public static void addDefautHeader(HttpUriRequest httpRequest) {
    httpRequest.addHeader(X_DOMAIN_NAME, DEFAULT_X_DOMAIN_NAME);
    httpRequest.addHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);
  }
}
