package com.huaweicloud.common.util;

import com.huawei.paas.foundation.auth.AuthHeaderProviderImpl;
import com.huaweicloud.common.transport.DealHeaderUtil;
import com.huaweicloud.common.transport.SSLConfig;
import com.huaweicloud.common.transport.ServiceCombSSLProperties;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.auth.SignRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @Author GuoYl123
 * @Date 2019/12/10
 **/
public class SecretUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(SecretUtil.class);
  //cse.credentials.project
  private static final String PAAS_PROJECT_NAME = "PAAS_PROJECT_NAME";

  public static final String ENDPOINT_PREFIX = "https://cse.";

  public static final String ENDPOINT_SUFFIX = ".myhuaweicloud.com";

  public static final String ENGINE_DATA_URL = "/cseengine/v1/engine-metadata?name=%s";

  public static SSLConfig generateSSLConfig(ServiceCombSSLProperties serviceCombSSLProperties) {
    SSLConfig sslConfig = new SSLConfig();
    Map<String, String> envHeaders = getAkSkFromSecret();
    sslConfig.setEnable(serviceCombSSLProperties.isEnable());
    if (envHeaders.containsKey(DealHeaderUtil.X_SERVICE_AK)) {
      sslConfig.setAccessKey(envHeaders.get(DealHeaderUtil.X_SERVICE_AK));
    } else {
      sslConfig.setAccessKey(serviceCombSSLProperties.getAccessKey());
    }
    if (envHeaders.containsKey(DealHeaderUtil.X_SERVICE_SHA_AKSK)) {
      sslConfig.setSecretKey(envHeaders.get(DealHeaderUtil.X_SERVICE_SHA_AKSK));
    } else {
      sslConfig.setSecretKey(serviceCombSSLProperties.getSecretKey());
    }
    sslConfig.setAkskCustomCipher(serviceCombSSLProperties.getAkskCustomCipher());
    if (envHeaders.containsKey(DealHeaderUtil.X_SERVICE_PROJECT)) {
      sslConfig.setProject(envHeaders.get(DealHeaderUtil.X_SERVICE_PROJECT));
    } else {
      sslConfig.setProject(serviceCombSSLProperties.getProject());
    }
    return sslConfig;
  }

  private static Map<String, String> getAkSkFromSecret() {
    String regionName = getRegionName();
    String endpoint = ENDPOINT_PREFIX + regionName + ENDPOINT_SUFFIX;
    String url = String.format(ENGINE_DATA_URL, null);
    AuthHeaderProvider authHeaderProvider = new AuthHeaderProviderImpl();
    SignRequest signRequest = AuthHeaderProviderImpl
        .createSignRequest("POST", endpoint + url, new HashMap<>(), null);
    Map<String, String> headers = authHeaderProvider.getSignAuthHeaders(signRequest);
    return headers;
  }

  private static String getRegionName() {
    SystemConfiguration sysConfig = new SystemConfiguration();
    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();
    String sysURL = sysConfig.getString(PAAS_PROJECT_NAME);
    String envURL = envConfig.getString(PAAS_PROJECT_NAME);
    return StringUtils.isEmpty(sysURL) ? envURL : sysURL;
  }
}
