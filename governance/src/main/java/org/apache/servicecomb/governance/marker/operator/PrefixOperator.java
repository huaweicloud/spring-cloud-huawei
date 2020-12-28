package org.apache.servicecomb.governance.marker.operator;

import org.springframework.stereotype.Component;

@Component
public class PrefixOperator implements MatchOperator {
  @Override
  public boolean match(String targetStr, String patternStr) {
    return targetStr.startsWith(patternStr);
  }
}
