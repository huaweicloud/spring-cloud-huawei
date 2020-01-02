package com.huaweicloud.common.util;

import com.huawei.paas.foundation.auth.AuthHeaderProviderImpl;
import com.huaweicloud.common.transport.DealHeaderUtil;
import com.huaweicloud.common.transport.AkSkConfig;
import com.huaweicloud.common.transport.ServiceCombSSLProperties;
import com.huaweicloud.common.transport.TLSConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;
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

  public static SSLContext getSSLContext(TLSConfig tlsConfig) {
    // create keyStore and trustStore
    KeyStore keyStore = getKeyStore(tlsConfig.getKeyStore(), tlsConfig.getKeyStoreType().name(),
        tlsConfig.getKeyStoreValue());
    KeyStore trustStore = getKeyStore(tlsConfig.getTrustStore(),
        TLSConfig.KeyStoreInstanceType.JKS.name(),
        tlsConfig.getTrustStoreValue());
    String keyStoreValue = tlsConfig.getKeyStoreValue();
    // initialize SSLContext
    try {
      KeyManagerFactory keyManagerFactory = KeyManagerFactory
          .getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keyStore, keyStoreValue.toCharArray());
      KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

      TrustManagerFactory trustManagerFactory = TrustManagerFactory
          .getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);
      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

      SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new TrustSelfSignedStrategy())
          .build();
      sslContext.init(keyManagers, trustManagers, new SecureRandom());
      return sslContext;
    } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static KeyStore getKeyStore(String keyStorePath, String keyStoreType,
      String keyStoreValue) {
    try {
      KeyStore keyStore = KeyStore.getInstance(keyStoreType);
      InputStream inputStream = new FileInputStream(keyStorePath);
      keyStore.load(inputStream, keyStoreValue.toCharArray());
      return keyStore;
    } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static AkSkConfig generateSSLConfig(ServiceCombSSLProperties serviceCombSSLProperties) {
    AkSkConfig akSkConfig = new AkSkConfig();
    Map<String, String> envHeaders = getAkSkFromSecret();
    String ak = envHeaders.containsKey(DealHeaderUtil.X_SERVICE_AK) ? envHeaders
        .get(DealHeaderUtil.X_SERVICE_AK) : serviceCombSSLProperties.getAccessKey();
    String sk = envHeaders.containsKey(DealHeaderUtil.X_SERVICE_SHA_AKSK) ? envHeaders
        .get(DealHeaderUtil.X_SERVICE_SHA_AKSK) : serviceCombSSLProperties.getSecretKey();
    String project = envHeaders.containsKey(DealHeaderUtil.X_SERVICE_PROJECT) ? envHeaders
        .get(DealHeaderUtil.X_SERVICE_PROJECT) : serviceCombSSLProperties.getProject();
    akSkConfig.setEnable(serviceCombSSLProperties.isEnable())
        .setAccessKey(ak)
        .setSecretKey(sk)
        .setAkskCustomCipher(serviceCombSSLProperties.getAkskCustomCipher())
        .setProject(project);
    return akSkConfig;
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
