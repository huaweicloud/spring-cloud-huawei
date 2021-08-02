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

import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;

public class TransportUtils {
  public static SSLProperties createSSLProperties(boolean sslEnabled,
      ServiceCombSSLProperties serviceCombSSLProperties) {
    SSLProperties sslProperties = new SSLProperties();
    sslProperties.setEnabled(sslEnabled);
    SSLOption sslOption = new SSLOption();
    sslOption.setKeyStoreType(serviceCombSSLProperties.getKeyStoreType() == null ?
        SSLOption.DEFAULT_OPTION.getKeyStoreType() : serviceCombSSLProperties.getKeyStoreType().name());
    sslOption.setKeyStore(serviceCombSSLProperties.getKeyStore() == null ?
        SSLOption.DEFAULT_OPTION.getKeyStore() : serviceCombSSLProperties.getKeyStore());
    sslOption.setKeyStoreValue(serviceCombSSLProperties.getKeyStoreValue() == null ?
        SSLOption.DEFAULT_OPTION.getKeyStoreValue() : serviceCombSSLProperties.getKeyStoreValue());
    sslOption.setTrustStoreType(serviceCombSSLProperties.getTrustStoreType() == null ?
        SSLOption.DEFAULT_OPTION.getTrustStoreType() : serviceCombSSLProperties.getTrustStoreType());
    sslOption.setTrustStore(serviceCombSSLProperties.getTrustStore() == null ?
        SSLOption.DEFAULT_OPTION.getTrustStore() : serviceCombSSLProperties.getTrustStore());
    sslOption.setTrustStoreValue(serviceCombSSLProperties.getTrustStoreValue() == null ?
        SSLOption.DEFAULT_OPTION.getTrustStoreValue() : serviceCombSSLProperties.getTrustStoreValue());
    sslOption.setCiphers(serviceCombSSLProperties.getCiphers() == null ?
        SSLOption.DEFAULT_OPTION.getCiphers() : serviceCombSSLProperties.getCiphers());
    sslOption.setProtocols(serviceCombSSLProperties.getProtocols() == null ?
        SSLOption.DEFAULT_OPTION.getProtocols() : serviceCombSSLProperties.getProtocols());
    sslOption.setEngine(serviceCombSSLProperties.getEngine() == null ?
        SSLOption.DEFAULT_OPTION.getEngine() : serviceCombSSLProperties.getEngine());
    sslOption.setCrl(serviceCombSSLProperties.getCrl() == null ? SSLOption.DEFAULT_OPTION.getCrl() :
        serviceCombSSLProperties.getCrl());
    sslOption.setCheckCNWhiteFile(serviceCombSSLProperties.getCheckCNWhiteFile() == null ?
        SSLOption.DEFAULT_OPTION.getCheckCNWhiteFile() : serviceCombSSLProperties.getCheckCNWhiteFile());
    sslOption.setStorePath(serviceCombSSLProperties.getStorePath() == null ?
        SSLOption.DEFAULT_OPTION.getStorePath() : serviceCombSSLProperties.getStorePath());
    sslOption.setSslCustomClass(serviceCombSSLProperties.getSslCustomClass() == null ?
        SSLOption.DEFAULT_OPTION.getSslCustomClass() : serviceCombSSLProperties.getSslCustomClass());
    sslOption.setAuthPeer(serviceCombSSLProperties.isAuthPeer());
    sslOption.setCheckCNHost(serviceCombSSLProperties.isCheckCNHost());
    sslOption.setAllowRenegociate(serviceCombSSLProperties.isAllowRenegociate());
    sslOption.setCheckCNWhite(serviceCombSSLProperties.isCheckCNWhite());

    sslProperties.setSslOption(sslOption);
    sslProperties.setSslCustom(SSLCustom.createSSLCustom(serviceCombSSLProperties.getSslCustomClass()));
    return sslProperties;
  }
}
