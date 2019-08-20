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

package org.springframework.cloud.servicecomb.discovery.client;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicecomb.discovery.client.exception.RemoteServerUnavailableException;
import org.springframework.cloud.servicecomb.discovery.client.model.Response;


/**
 * @Author wangqijun
 * @Date 11:21 2019-07-08
 **/
public class DefaultHttpTransport implements HttpTransport {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpTransport.class);

  private static final DefaultHttpTransport DEFAULT_HTTP_TRANSPORT = new DefaultHttpTransport();

  public static final int CONNECT_TIMEOUT = 5000;

  public static final int CONNECTION_REQUEST_TIMEOUT = 5000;

  public static final int SOCKET_TIMEOUT = 5000;

  public static final int MAX_TOTAL = 1000;

  public static final int DEFAULT_MAX_PER_ROUTE = 500;

  public static final String X_DOMAIN_NAME = "x-domain-name";

  public static final String DEFAULT_X_DOMAIN_NAME = "default";

  public static final String CONTENT_TYPE = "Content-type";

  public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

  private HttpClient httpClient;

  private DefaultHttpTransport() {
    RequestConfig config = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setConnectionRequestTimeout(
        CONNECTION_REQUEST_TIMEOUT)
        .setSocketTimeout(SOCKET_TIMEOUT).build();
    PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
    manager.setMaxTotal(MAX_TOTAL);
    manager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().setConnectionManager(manager)
        .setDefaultRequestConfig(config);
    this.httpClient = httpClientBuilder.build();
  }

  public static DefaultHttpTransport getInstance() {
    return DEFAULT_HTTP_TRANSPORT;
  }

  @Override
  public Response execute(HttpUriRequest httpRequest) throws RemoteServerUnavailableException {
    Response resp = new Response();
    try {
      httpRequest.addHeader(X_DOMAIN_NAME, DEFAULT_X_DOMAIN_NAME);
      httpRequest.addHeader(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON);
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

  @Override
  public Response sendPutRequest(String url, HttpEntity httpEntity) throws RemoteServerUnavailableException {
    HttpPut httpPut = new HttpPut(url);
    httpPut.setEntity(httpEntity);
    return this.execute(httpPut);
  }

  @Override
  public Response sendPostRequest(String url, HttpEntity httpEntity) throws RemoteServerUnavailableException {
    HttpPost httpPost = new HttpPost(url);
    httpPost.setEntity(httpEntity);
    return this.execute(httpPost);
  }

  @Override
  public Response sendDeleteRequest(String url) throws RemoteServerUnavailableException {
    HttpDelete httpDelete = new HttpDelete(url);
    return this.execute(httpDelete);
  }
}
