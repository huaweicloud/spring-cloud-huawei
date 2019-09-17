package org.springframework.cloud.dtm.rest;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class RestTemplateForDtmInterceptor implements ClientHttpRequestInterceptor {

  public static final String CSE_DTM_CONTEXT = "cseDtmContext";

  private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateForDtmInterceptor.class);

  @Override
  public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
      ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
    DTMContext dtmContext = DTMContext.getDTMContext();
    long gid = dtmContext.getGlobalTxId();
    LOGGER.info("gid:" + gid);
    HttpHeaders headers = httpRequest.getHeaders();
    List<String> gidHeaders = headers.get(CSE_DTM_CONTEXT);
    //consumer side
    if (gidHeaders.size() > 0) {
      CseDtmContext cseDtmContext = Json.decodeValue(gidHeaders.get(0), CseDtmContext.class);
      LOGGER.info("consumer cseDtmContext:" + cseDtmContext);
      transform(dtmContext, cseDtmContext);
    } else if (gid != -1) {//provider side
      CseDtmContext cseDtmContext = CseDtmContext.fromDtmContext(dtmContext);
      LOGGER.info("provider cseDtmContext:" + cseDtmContext);
      headers.add(CSE_DTM_CONTEXT, Json.encode(cseDtmContext));
    }

    return clientHttpRequestExecution.execute(httpRequest, bytes);
  }

  private void transform(DTMContext dtmContext, CseDtmContext cseDtmContext) {
    dtmContext.setGlobalTxId(cseDtmContext.getGlobalTxId());
    dtmContext.setBranchTxId(cseDtmContext.getBranchTxId());
    dtmContext.setBranchOptionalData(cseDtmContext.getBranchOptionalData());
    dtmContext.setGlobalOptionalData(cseDtmContext.getGlobalOptionalData());
    dtmContext.setChannelKey(cseDtmContext.getChannelKey());
  }
}
