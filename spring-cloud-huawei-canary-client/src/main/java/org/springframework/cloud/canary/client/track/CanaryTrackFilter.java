package org.springframework.cloud.canary.client.track;

import javax.servlet.*;
import java.io.IOException;
import java.util.UUID;

/**
 * @Author GuoYl123
 * @Date 2019/10/12
 **/
public class CanaryTrackFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        CanaryTrackRequest canaryTrackRequest = new CanaryTrackRequest(UUID.randomUUID().toString());
        CanaryTrackContext.setRequestInfo(canaryTrackRequest);
        filterChain.doFilter(servletRequest, servletResponse);
        CanaryTrackContext.removeRequestInfo();
    }
}
