package org.apache.servicecomb.governance.marker.operator;

import org.springframework.stereotype.Component;

@Component
public class SuffixOperator implements MatchOperator {
  @Override
  public boolean match(String targetStr, String patternStr) {
    return targetStr.endsWith(patternStr);
  }
}
