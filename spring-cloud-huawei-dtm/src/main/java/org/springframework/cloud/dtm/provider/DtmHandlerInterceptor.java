package org.springframework.cloud.dtm.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.dtm.util.DtmConstants;
import org.springframework.web.servlet.HandlerInterceptor;

import com.huawei.middleware.dtm.client.context.DTMContext;
import com.huawei.paas.dtm.servicecomb.context.CseDtmContext;

import io.vertx.core.json.Json;

/**
 * Implement filter, convert the data before the request is processed, before being processed by the business
 * @Author wangqijun
 * @Date 17:21 2019-09-25
 **/
public class DtmHandlerInterceptor implements HandlerInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(DtmHandlerInterceptor.class);

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    //get header then fill to DTMContext for business use it
    DTMContext dtmContext = DTMContext.getDTMContext();
    String dtmHeader = request.getHeader(DtmConstants.DTM_CONTEXT);
    if (StringUtils.isNotEmpty(dtmHeader)) {
      CseDtmContext cseDtmContext = Json.decodeValue(dtmHeader, CseDtmContext.class);
      LOGGER.debug("dtm info, provider cseDtmContext:" + cseDtmContext);
      transform(dtmContext, cseDtmContext);
    }
    return true;
  }

  /**
   * transform CseDtmContext to DTMContext
   * @param dtmContext
   * @param cseDtmContext
   */
  private void transform(DTMContext dtmContext, CseDtmContext cseDtmContext) {
    dtmContext.setGlobalTxId(cseDtmContext.getGlobalTxId());
    dtmContext.setBranchTxId(cseDtmContext.getBranchTxId());
    dtmContext.setBranchOptionalData(cseDtmContext.getBranchOptionalData());
    dtmContext.setGlobalOptionalData(cseDtmContext.getGlobalOptionalData());
    dtmContext.setChannelKey(cseDtmContext.getChannelKey());
  }
}
