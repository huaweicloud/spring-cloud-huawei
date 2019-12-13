package com.huaweicloud.router.client.track;

import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2019/12/12
 **/
public interface RouterHeaderFilterExt extends Comparable<RouterHeaderFilterExt> {

  default int getOrder() {
    return 0;
  }

  default boolean enabled() {
    return true;
  }

  Map<String, String> doFilter(Map<String, String> invokeHeader);

  @Override
  default int compareTo(RouterHeaderFilterExt o) {
    return Integer.compare(this.getOrder(), o.getOrder());
  }
}
