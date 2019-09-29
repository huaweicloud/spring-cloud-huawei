package org.springframework.cloud.dtm.provider;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.cloud.dtm.DtmContextDTO;
import org.springframework.cloud.dtm.util.DtmConstants;

import com.huawei.middleware.dtm.client.context.DTMContext;

import io.vertx.core.json.Json;
import mockit.Expectations;
import mockit.Injectable;

/**
 * @Author wangqijun
 * @Date 10:45 2019-09-29
 **/
public class DtmHandlerInterceptorTest {

  @Test
  public void preHandle(@Injectable HttpServletRequest request) throws Exception {
    DtmHandlerInterceptor dtmHandlerInterceptor = new DtmHandlerInterceptor();
    DtmContextDTO dtmContextDTO = new DtmContextDTO();
    dtmContextDTO.setGlobalTxId(100);
    new Expectations() {
      {
        request.getHeader(DtmConstants.DTM_CONTEXT);
        result = Json.encode(dtmContextDTO);
      }
    };
    dtmHandlerInterceptor.preHandle(request, null, null);

    assertEquals(DTMContext.getDTMContext().getGlobalTxId(), 100);
  }
}