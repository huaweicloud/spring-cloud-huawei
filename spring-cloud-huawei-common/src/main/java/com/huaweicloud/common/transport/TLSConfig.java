package com.huaweicloud.common.transport;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @Author GuoYl123
 * @Date 2019/12/28
 **/
@Component
@ConfigurationProperties("spring.cloud.servicecomb.ssl")
public class TLSConfig {

  private KeyStoreInstanceType keyStoreType;

  //trust certificate
  private String trustStore;

  //trust certificate password
  private String trustStoreValue;

  //identity certificate
  private String keyStore;

  //identity certificate password
  private String keyStoreValue;

  public KeyStoreInstanceType getKeyStoreType() {
    return keyStoreType;
  }

  public void setKeyStoreType(KeyStoreInstanceType keyStoreType) {
    this.keyStoreType = keyStoreType;
  }

  public String getTrustStore() {
    return trustStore;
  }

  public void setTrustStore(String trustStore) {
    this.trustStore = trustStore;
  }

  public String getTrustStoreValue() {
    return trustStoreValue;
  }

  public void setTrustStoreValue(String trustStoreValue) {
    this.trustStoreValue = trustStoreValue;
  }

  public String getKeyStore() {
    return keyStore;
  }

  public void setKeyStore(String keyStore) {
    this.keyStore = keyStore;
  }

  public String getKeyStoreValue() {
    return keyStoreValue;
  }

  public void setKeyStoreValue(String keyStoreValue) {
    this.keyStoreValue = keyStoreValue;
  }

  public boolean isEmpty() {
    return StringUtils.isEmpty(trustStoreValue) || StringUtils.isEmpty(keyStoreValue) || StringUtils
        .isEmpty(trustStore) || StringUtils.isEmpty(keyStore);
  }

  //keyStore type
  public enum KeyStoreInstanceType {
    JKS,
    JCEKS,
    PKCS12,
    PKCS11,
    DKS
  }
}
