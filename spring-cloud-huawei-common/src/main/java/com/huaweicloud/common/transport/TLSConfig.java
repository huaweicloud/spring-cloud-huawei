package com.huaweicloud.common.transport;

/**
 * @Author GuoYl123
 * @Date 2019/12/28
 **/
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

  //keyStore type
  public enum KeyStoreInstanceType {
    JKS,
    JCEKS,
    PKCS12,
    PKCS11,
    DKS
  }
}
