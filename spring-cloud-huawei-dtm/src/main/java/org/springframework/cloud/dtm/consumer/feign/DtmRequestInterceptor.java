package org.springframework.cloud.dtm.consumer.feign;

import org.springframework.cloud.dtm.util.DtmConstants;

import com.huawei.middleware.dtm.client.context.DTMContext;
import com.huawei.paas.dtm.servicecomb.context.CseDtmContext;

import io.vertx.core.json.Json;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * @Author wangqijun
 * @Date 09:52 2019-09-26
 **/
public class DtmRequestInterceptor implements RequestInterceptor {
  @Override
  public void apply(RequestTemplate template) {
    DTMContext dtmContext = DTMContext.getDTMContext();
    long gid = dtmContext.getGlobalTxId();
    if (gid != -1) {
      CseDtmContext cseDtmContext = CseDtmContext.fromDtmContext(dtmContext);
      template.header(DtmConstants.DTM_CONTEXT, Json.encode(cseDtmContext));
    }
  }
}
