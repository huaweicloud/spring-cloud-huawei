package com.huaweicloud.dtm.consumer.feign;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import com.huaweicloud.dtm.util.DtmConstants;

import com.huawei.middleware.dtm.client.context.DTMContext;

import feign.RequestTemplate;

/**
 * @Author wangqijun
 * @Date 10:52 2019-09-29
 **/
public class DtmRequestInterceptorTest {

  @Test
  public void apply() {
    DtmRequestInterceptor dtmRequestInterceptor = new DtmRequestInterceptor();
    RequestTemplate template = new RequestTemplate();
    DTMContext dtmContext = DTMContext.getDTMContext();
    dtmContext.setGlobalTxId(100);
    dtmRequestInterceptor.apply(template);
    assertNotNull(template.header(DtmConstants.DTM_CONTEXT));
  }
}