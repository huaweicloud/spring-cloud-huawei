package org.springframework.cloud.dtm.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.dtm.DtmContextDTO;
import org.springframework.cloud.dtm.util.DtmConstants;
import org.springframework.web.servlet.HandlerInterceptor;

import com.huawei.middleware.dtm.client.context.DTMContext;

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
      DtmContextDTO dtmContextDTO = Json.decodeValue(dtmHeader, DtmContextDTO.class);
      LOGGER.debug("dtm info, provider dtmContextDTO:" + dtmContextDTO);
      transform(dtmContext, dtmContextDTO);
    }
    return true;
  }

  /**
   * transform DtmContextDTO to DTMContext
   * @param dtmContext
   * @param dtmContextDTO
   */
  private void transform(DTMContext dtmContext, DtmContextDTO dtmContextDTO) {
    dtmContext.setGlobalTxId(dtmContextDTO.getGlobalTxId());
    dtmContext.setBranchTxId(dtmContextDTO.getBranchTxId());
    dtmContext.setBranchOptionalData(dtmContextDTO.getBranchOptionalData());
    dtmContext.setGlobalOptionalData(dtmContextDTO.getGlobalOptionalData());
    dtmContext.setChannelKey(dtmContextDTO.getChannelKey());
  }
}
