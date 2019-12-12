package com.huaweicloud.router.client.feign;

import com.huaweicloud.router.client.header.HeaderPassUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2019/12/12
 **/
public class RouterRequestInterceptor implements RequestInterceptor {

  private static final String ROUTER_HEADER = "X-RouterContext";

  /**
   * header pass
   *
   * @param requestTemplate
   */
  @Override
  public void apply(RequestTemplate requestTemplate) {
    Map<String, String> allHeaders = new HashMap<>();
    requestTemplate.headers().forEach((k, v) -> {
      allHeaders.put(k, v.toArray()[0].toString());
    });
    requestTemplate.header(ROUTER_HEADER, HeaderPassUtil.getPassHeaderString(allHeaders));
  }
}
