package com.huaweicloud.dtm.consumer.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;

import org.junit.Test;
import com.huaweicloud.dtm.DtmContextDTO;
import com.huaweicloud.dtm.util.DtmConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.huawei.middleware.dtm.client.context.DTMContext;

import io.vertx.core.json.Json;

/**
 * @Author wangqijun
 * @Date 18:04 2019-09-27
 **/
public class DtmRestTemplateInterceptorTest {

  private final ClientHttpRequestInterceptor clientHttpRequestInterceptor = new DtmRestTemplateInterceptor();
  @Test
  public void intercept() throws IOException {
    DTMContext dtmContext = DTMContext.getDTMContext();
    int expectGlobalTxId = 100;
    dtmContext.setGlobalTxId(expectGlobalTxId);
    ClientHttpRequestExecution clientHttpRequestExecution = new ClientHttpRequestExecution() {
      @Override
      public ClientHttpResponse execute(HttpRequest request, byte[] body) throws IOException {
        return null;
      }
    };
    HttpHeaders header = new HttpHeaders();
    HttpRequest request = new HttpRequest() {
      @Override
      public String getMethodValue() {
        return null;
      }

      @Override
      public URI getURI() {
        return null;
      }

      @Override
      public HttpHeaders getHeaders() {
        return header;
      }
    };
    clientHttpRequestInterceptor.intercept(request, null, clientHttpRequestExecution);
    DtmContextDTO dtmContextDTO = Json.decodeValue(header.get(DtmConstants.DTM_CONTEXT).get(0), DtmContextDTO.class);
    assertEquals(dtmContextDTO.getGlobalTxId(), expectGlobalTxId);
  }

}