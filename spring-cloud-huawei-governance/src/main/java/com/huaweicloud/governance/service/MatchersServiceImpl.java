package com.huaweicloud.governance.service;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import com.huaweicloud.governance.marker.GovHttpRequest;
import com.huaweicloud.governance.properties.MatchProperties;
import com.huaweicloud.governance.marker.Matcher;
import com.huaweicloud.governance.marker.RequestProcessor;
import com.huaweicloud.governance.marker.TrafficMarker;

public class MatchersServiceImpl implements MatchersService {

  @Autowired
  private RequestProcessor requestProcessor;

  @Autowired
  private MatchProperties matchProperties;

  /**
   * @param govHttpRequest
   * @return
   */
  @Override
  public String getMatchStr(GovHttpRequest govHttpRequest) {
    Map<String, TrafficMarker> map = matchProperties.covert();
    String mark = null;
    for (Entry<String, TrafficMarker> entry : map.entrySet()) {
      boolean isMatch = true;
      for (Matcher match : entry.getValue().getMatches()) {
        if (!requestProcessor.match(govHttpRequest, match)) {
          isMatch = false;
          break;
        }
      }
      if (isMatch) {
        mark = entry.getKey();
        break;
      }
    }
    return mark;
  }
}
