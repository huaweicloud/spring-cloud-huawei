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

import com.huaweicloud.common.cache.TokenCache;
import com.huaweicloud.common.util.SecretUtil;

import java.io.IOException;

import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import com.huaweicloud.common.exception.RemoteServerUnavailableException;
import com.huaweicloud.common.util.URLUtil;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;


/**
 * @Author wangqijun
 * @Date 11:21 2019-07-08
 **/
public class DefaultHttpTransport implements HttpTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpTransport.class);

  private volatile static DefaultHttpTransport DEFAULT_HTTP_TRANSPORT;

  private ServiceCombAkSkProperties serviceCombAkSkProperties;

  private HttpClient httpClient;

  public void setRBACToken(ServiceCombRBACProperties serviceCombRBACProperties,
      String urls) {
    if (StringUtils.isEmpty(serviceCombRBACProperties.getName()) ||
        StringUtils.isEmpty(serviceCombRBACProperties.getPassword())) {
      return;
    }
    List<String> urlList = URLUtil.dealMultiUrl(urls);
    getToken(serviceCombRBACProperties, urlList);
  }

  private void getToken(ServiceCombRBACProperties serviceCombRBACProperties, List<String> urlList) {
    Response response = null;
    String url;
    for (String s : urlList) {
      url = s;
      try {
        StringEntity stringEntity = new StringEntity(
            JsonUtils.OBJ_MAPPER.writeValueAsString(serviceCombRBACProperties), "utf-8");
        response = this.sendPostRequest(url + "/v4/token", stringEntity);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
          RBACToken token = JsonUtils.OBJ_MAPPER
              .readValue(response.getContent(), RBACToken.class);
          LOGGER.info("get token success.");
          TokenCache.setToken(token.getToken());
          break;
        } else {
          LOGGER.error("response failed. status:" + response.getStatusCode() + "; message:" + response
              .getStatusMessage() + "; content:" + response.getContent());
        }
      } catch (RemoteServerUnavailableException e) {
        LOGGER.error(url + " response failed. status:" + response.getStatusCode() + "; message:" + response
            .getStatusMessage() + "; content:" + response.getContent());
      } catch (IOException e) {
        LOGGER.warn("parse result failed, {}", response);
      }
    }
  }


  private void addToken(HttpUriRequest httpRequest) {
    if (!StringUtils.isEmpty(TokenCache.getToken())) {
      httpRequest.addHeader("Authorization", "Bearer " + TokenCache.getToken());
    }
  }

  private DefaultHttpTransport(ServiceCombSSLProperties serviceCombSSLProperties) {
    SSLContext sslContext = SecretUtil.getSSLContext(serviceCombSSLProperties);

    RequestConfig config = RequestConfig.custom()
        .setConnectTimeout(DealHeaderUtil.CONNECT_TIMEOUT)
        .setConnectionRequestTimeout(
            DealHeaderUtil.CONNECTION_REQUEST_TIMEOUT)
        .setSocketTimeout(DealHeaderUtil.SOCKET_TIMEOUT).build();

    //register http/https socket factory
    Registry<ConnectionSocketFactory> connectionSocketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("http", PlainConnectionSocketFactory.INSTANCE)
        .register("https",
            new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
        .build();

    //connection pool management
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
        connectionSocketFactoryRegistry);
    connectionManager.setMaxTotal(DealHeaderUtil.MAX_TOTAL);
    connectionManager.setDefaultMaxPerRoute(DealHeaderUtil.DEFAULT_MAX_PER_ROUTE);

    // construct httpClient
    // delete before code : setSSLHostnameVerifier(hostnameVerifier)
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().
        setDefaultRequestConfig(config).
        setConnectionManager(connectionManager).
        disableCookieManagement();

    this.httpClient = httpClientBuilder.build();
  }

  public static DefaultHttpTransport getInstance(
      ServiceCombSSLProperties serviceCombSSLProperties) {
    if (null == DEFAULT_HTTP_TRANSPORT) {
      synchronized (DefaultHttpTransport.class) {
        if (null == DEFAULT_HTTP_TRANSPORT) {
          DEFAULT_HTTP_TRANSPORT = new DefaultHttpTransport(serviceCombSSLProperties);
        }
      }
    }
    return DEFAULT_HTTP_TRANSPORT;
  }

  @Override
  public Response execute(HttpUriRequest httpRequest) throws RemoteServerUnavailableException {
    Response resp = new Response();
    try {
      DealHeaderUtil.addDefautHeader(httpRequest);
      DealHeaderUtil.addAKSKHeader(httpRequest, serviceCombAkSkProperties);
      addToken(httpRequest);
      HttpResponse httpResponse = httpClient.execute(httpRequest);
      resp.setStatusCode(httpResponse.getStatusLine().getStatusCode());
      resp.setStatusMessage(httpResponse.getStatusLine().getReasonPhrase());
      resp.setHeaders(httpResponse.getAllHeaders());
      if (httpResponse.getEntity() != null) {
        resp.setContent(EntityUtils.toString(httpResponse.getEntity()));
      }
    } catch (IOException e) {
      throw new RemoteServerUnavailableException(
          "server is unavailable. message=" + e.getMessage(), e);
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
  public void setServiceCombAkSkProperties(ServiceCombAkSkProperties serviceCombAkSkProperties) {
    this.serviceCombAkSkProperties = serviceCombAkSkProperties;
  }
}
