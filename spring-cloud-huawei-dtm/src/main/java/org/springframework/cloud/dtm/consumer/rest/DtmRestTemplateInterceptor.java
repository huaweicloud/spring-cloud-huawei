package org.springframework.cloud.dtm.consumer.rest;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.dtm.util.DtmConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.huawei.middleware.dtm.client.context.DTMContext;
import com.huawei.paas.dtm.servicecomb.context.CseDtmContext;

import io.vertx.core.json.Json;

/**
 * @Author wangqijun
 * @Date 10:08 2019-09-18
 **/
public class DtmRestTemplateInterceptor implements ClientHttpRequestInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DtmRestTemplateInterceptor.class);

  @Override
  public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
      ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
    DTMContext dtmContext = DTMContext.getDTMContext();
    long gid = dtmContext.getGlobalTxId();
    HttpHeaders headers = httpRequest.getHeaders();
    if (gid != -1) {
      CseDtmContext cseDtmContext = CseDtmContext.fromDtmContext(dtmContext);
      headers.add(DtmConstants.DTM_CONTEXT, Json.encode(cseDtmContext));
    }
    return clientHttpRequestExecution.execute(httpRequest, bytes);
  }

}
