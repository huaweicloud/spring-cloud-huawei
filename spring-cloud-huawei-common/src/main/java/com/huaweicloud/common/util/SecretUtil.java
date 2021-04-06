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

package com.huaweicloud.common.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.common.transport.ServiceCombSSLProperties;

/**
 * @Author GuoYl123
 * @Date 2019/12/10
 **/
public class SecretUtil {
  public static final String HMAC_SHA_256 = "HmacSHA256";

  private static final Logger LOGGER = LoggerFactory.getLogger(SecretUtil.class);

  public static SSLContext getSSLContext(ServiceCombSSLProperties serviceCombSSLProperties) {
    if (serviceCombSSLProperties == null || serviceCombSSLProperties.isEmpty()) {
      SSLContext sslContext = null;
      try {
        sslContext = new SSLContextBuilder()
            .loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true).build();
      } catch (Exception e) {
        LOGGER.info(e.getMessage(), e);
      }
      return sslContext;
    }
    // create keyStore and trustStore
    KeyStore keyStore = getKeyStore(
        serviceCombSSLProperties.getKeyStore(), serviceCombSSLProperties.getKeyStoreType().name(),
        serviceCombSSLProperties.getKeyStoreValue());
    KeyStore trustStore = getKeyStore(serviceCombSSLProperties.getTrustStore(),
        ServiceCombSSLProperties.KeyStoreInstanceType.JKS.name(),
        serviceCombSSLProperties.getTrustStoreValue());
    String keyStoreValue = serviceCombSSLProperties.getKeyStoreValue();
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
      LOGGER.error("generate ssl context failed:{}", e.getMessage());
    }
    return null;
  }

  private static KeyStore getKeyStore(String keyStorePath, String keyStoreType,
      String keyStoreValue) {
    try {
      KeyStore keyStore = KeyStore.getInstance(keyStoreType);
      String path = URLDecoder
          .decode(SecretUtil.class.getResource("/" + keyStorePath).getFile(), "utf-8");
      InputStream inputStream = new FileInputStream(path);
      keyStore.load(inputStream, keyStoreValue.toCharArray());
      return keyStore;
    } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
      LOGGER.error("parse keyStore file failed:{}", e.getMessage());
    }
    return null;
  }

  public static String sha256Encode(String key, String data) {
    try {
      Mac sha256HMAC = Mac.getInstance(HMAC_SHA_256);
      SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),
          HMAC_SHA_256);
      sha256HMAC.init(secretKey);
      return Hex.encodeHexString(sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }
}
