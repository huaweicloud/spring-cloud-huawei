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

import com.huaweicloud.common.util.SecretUtil;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huaweicloud.common.exception.RemoteServerUnavailableException;
import org.springframework.util.StringUtils;


/**
 * @Author wangqijun
 * @Date 11:21 2019-07-08
 **/
public class DefaultHttpTransport implements HttpTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpTransport.class);

  private static final DefaultHttpTransport DEFAULT_HTTP_TRANSPORT = new DefaultHttpTransport();

  private SSLConfig sslConfig;

  private HttpClient httpClient;

  private DefaultHttpTransport(TLSConfig tLSConfig) {
    SSLContext sslContext = SecretUtil.getSSLContext(tLSConfig);

    RequestConfig config = RequestConfig.custom()
        .setConnectTimeout(DealHeaderUtil.CONNECT_TIMEOUT)
        .setConnectionRequestTimeout(
            DealHeaderUtil.CONNECTION_REQUEST_TIMEOUT)
        .setSocketTimeout(DealHeaderUtil.SOCKET_TIMEOUT).build();

    //register http/https socket factory
    Registry<ConnectionSocketFactory> connectionSocketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("http", PlainConnectionSocketFactory.INSTANCE)
        .register("https", new SSLConnectionSocketFactory(sslContext))
        .build();

    //connection pool management
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
        connectionSocketFactoryRegistry);
    connectionManager.setMaxTotal(1000);
    connectionManager.setDefaultMaxPerRoute(500);

    // construct httpClient
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().
        setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext)).
        setDefaultRequestConfig(config).
        setConnectionManager(connectionManager).
        disableCookieManagement();

    this.httpClient = httpClientBuilder.build();
  }

  private DefaultHttpTransport() {
    SSLContext sslContext = null;
    try {
      sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
        //信任所有
        public boolean isTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
          return true;
        }
      }).build();
    } catch (NoSuchAlgorithmException e) {
      LOGGER.info(e.getMessage(), e);
    } catch (KeyManagementException e) {
      LOGGER.info(e.getMessage(), e);
    } catch (KeyStoreException e) {
      LOGGER.info(e.getMessage(), e);
    }

    RequestConfig config = RequestConfig.custom().setConnectTimeout(DealHeaderUtil.CONNECT_TIMEOUT)
        .setConnectionRequestTimeout(
            DealHeaderUtil.CONNECTION_REQUEST_TIMEOUT)
        .setSocketTimeout(DealHeaderUtil.SOCKET_TIMEOUT).build();

    HostnameVerifier hostnameVerifier = new HostnameVerifier() {
      @Override
      public boolean verify(String s, SSLSession sslSession) {
        return true;
      }
    };
    this.httpClient = HttpClients.custom().setSSLHostnameVerifier(hostnameVerifier)
        .setSSLContext(sslContext)
        .setDefaultRequestConfig(config).disableCookieManagement().build();
  }

  public static DefaultHttpTransport getInstance() {
    return DEFAULT_HTTP_TRANSPORT;
  }

  @Override
  public Response execute(HttpUriRequest httpRequest) throws RemoteServerUnavailableException {
    Response resp = new Response();
    try {
      DealHeaderUtil.addDefautHeader(httpRequest);
      DealHeaderUtil.addAKSKHeader(httpRequest, sslConfig);
      HttpResponse httpResponse = httpClient.execute(httpRequest);
      resp.setStatusCode(httpResponse.getStatusLine().getStatusCode());
      resp.setStatusMessage(httpResponse.getStatusLine().getReasonPhrase());
      resp.setContent(EntityUtils.toString(httpResponse.getEntity()));
    } catch (IOException e) {
      throw new RemoteServerUnavailableException(
          "service center unavailable. message=" + e.getMessage(), e);
    }
    return resp;
  }


  @Override
  public Response sendGetRequest(String url) throws RemoteServerUnavailableException {
    HttpGet httpGet = new HttpGet(url);
    return this.execute(httpGet);
  }

  public Response sendGetRequest(String url, Map<String, String> headers)
      throws RemoteServerUnavailableException {
    HttpGet httpGet = new HttpGet(url);
    headers.forEach((k, v) -> {
          if (!StringUtils.isEmpty(k)) {
            httpGet.addHeader(k, v);
          }
        }
    );
    return this.execute(httpGet);
  }

  @Override
  public Response sendPutRequest(String url, HttpEntity httpEntity)
      throws RemoteServerUnavailableException {
    HttpPut httpPut = new HttpPut(url);
    httpPut.setEntity(httpEntity);
    return this.execute(httpPut);
  }

  @Override
  public Response sendPostRequest(String url, HttpEntity httpEntity)
      throws RemoteServerUnavailableException {
    HttpPost httpPost = new HttpPost(url);
    httpPost.setEntity(httpEntity);
    return this.execute(httpPost);
  }

  @Override
  public Response sendDeleteRequest(String url) throws RemoteServerUnavailableException {
    HttpDelete httpDelete = new HttpDelete(url);
    return this.execute(httpDelete);
  }

  @Override
  public void setSslConfig(SSLConfig sslConfig) {
    this.sslConfig = sslConfig;
  }
}
