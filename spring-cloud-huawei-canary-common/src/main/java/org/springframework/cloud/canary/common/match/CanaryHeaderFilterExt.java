package org.springframework.cloud.canary.common.match;

import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public interface CanaryHeaderFilterExt {
    Map<String, String> doFilter(Map<String, String> invokeHeader);
}
