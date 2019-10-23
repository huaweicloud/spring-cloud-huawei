package org.springframework.cloud.canary.client.track;

import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;

import org.springframework.lang.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Map;
import java.util.UUID;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class CanaryHandlerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        CanaryTrackContext.setRequestInfo( new CanaryTrackRequest(UUID.randomUUID().toString()));
        CanaryTrackContext.setRequestHeader(getHeaders(request));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        CanaryTrackContext.removeRequestInfo();
    }

    private static Map<String, String> getHeaders(HttpServletRequest servletRequest) {
        Enumeration<String> headerNames = servletRequest.getHeaderNames();
        HttpHeaders httpHeaders = new HttpHeaders();
        while (headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = servletRequest.getHeaders(headerName);
            while (headerValues.hasMoreElements()){
                httpHeaders.add(headerName, headerValues.nextElement());
            }
        }
        return httpHeaders.toSingleValueMap();
    }
}
