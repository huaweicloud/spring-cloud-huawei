/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
public class ServiceCombSSLProperties {

  private KeyStoreInstanceType keyStoreType;

  //trust certificate
  private String trustStore;

  //trust certificate password
  private String trustStoreValue;

  //identity certificate
  private String keyStore;

  //identity certificate password
  private String keyStoreValue;

  private String sslCustomClass;

  private String ciphers;

  private String engine;

  private String protocols;

  private boolean authPeer;

  private boolean checkCNHost;

  private boolean checkCNWhite;

  private String checkCNWhiteFile;

  private boolean allowRenegociate;

  private String storePath;

  private String trustStoreType;

  private String crl;

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

  public String getSslCustomClass() {
    return sslCustomClass;
  }

  public void setSslCustomClass(String sslCustomClass) {
    this.sslCustomClass = sslCustomClass;
  }

  public String getCiphers() {
    return ciphers;
  }

  public void setCiphers(String ciphers) {
    this.ciphers = ciphers;
  }

  public String getEngine() {
    return engine;
  }

  public void setEngine(String engine) {
    this.engine = engine;
  }

  public String getProtocols() {
    return protocols;
  }

  public void setProtocols(String protocols) {
    this.protocols = protocols;
  }

  public boolean isAuthPeer() {
    return authPeer;
  }

  public void setAuthPeer(boolean authPeer) {
    this.authPeer = authPeer;
  }

  public boolean isCheckCNHost() {
    return checkCNHost;
  }

  public void setCheckCNHost(boolean checkCNHost) {
    this.checkCNHost = checkCNHost;
  }

  public boolean isCheckCNWhite() {
    return checkCNWhite;
  }

  public void setCheckCNWhite(boolean checkCNWhite) {
    this.checkCNWhite = checkCNWhite;
  }

  public String getCheckCNWhiteFile() {
    return checkCNWhiteFile;
  }

  public void setCheckCNWhiteFile(String checkCNWhiteFile) {
    this.checkCNWhiteFile = checkCNWhiteFile;
  }

  public boolean isAllowRenegociate() {
    return allowRenegociate;
  }

  public void setAllowRenegociate(boolean allowRenegociate) {
    this.allowRenegociate = allowRenegociate;
  }

  public String getStorePath() {
    return storePath;
  }

  public void setStorePath(String storePath) {
    this.storePath = storePath;
  }

  public String getTrustStoreType() {
    return trustStoreType;
  }

  public void setTrustStoreType(String trustStoreType) {
    this.trustStoreType = trustStoreType;
  }

  public String getCrl() {
    return crl;
  }

  public void setCrl(String crl) {
    this.crl = crl;
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
