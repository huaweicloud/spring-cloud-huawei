package org.springframework.cloud.canary.client.track;

import org.springframework.web.servlet.HandlerInterceptor;

import org.springframework.lang.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class CanaryHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        CanaryTrackContext.setRequestInfo( new CanaryTrackRequest(UUID.randomUUID().toString()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        CanaryTrackContext.removeRequestInfo();
    }
}
