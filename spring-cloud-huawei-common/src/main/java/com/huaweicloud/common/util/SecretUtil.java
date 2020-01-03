package com.huaweicloud.common.util;

import com.huaweicloud.common.exception.ServiceCombRuntimeException;
import com.huaweicloud.common.transport.AkSkConfig;
import com.huaweicloud.common.transport.ServiceCombAkSkProperties;
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
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;
import org.springframework.util.StringUtils;

/**
 * @Author GuoYl123
 * @Date 2019/12/10
 **/
public class SecretUtil {

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

  public static AkSkConfig generateSSLConfig(ServiceCombAkSkProperties serviceCombAkSkProperties) {
    if (!StringUtils.isEmpty(serviceCombAkSkProperties.getEnable())) {
      throw new ServiceCombRuntimeException(
          "config credentials.enable has change to credentials.enabled ,old names are no longer supported, please change it.");
    }
    AkSkConfig akSkConfig = new AkSkConfig();
    akSkConfig.setEnabled(serviceCombAkSkProperties.isEnabled())
        .setAccessKey(serviceCombAkSkProperties.getAccessKey())
        .setSecretKey(serviceCombAkSkProperties.getSecretKey())
        .setAkskCustomCipher(serviceCombAkSkProperties.getAkskCustomCipher())
        .setProject(serviceCombAkSkProperties.getProject());
    return akSkConfig;
  }
}
