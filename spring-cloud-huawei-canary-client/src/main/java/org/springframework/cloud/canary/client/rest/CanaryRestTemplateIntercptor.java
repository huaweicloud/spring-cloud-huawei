package org.springframework.cloud.canary.client.rest;

import org.springframework.cloud.canary.client.track.CanaryTrackContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @Author GuoYl123
 * @Date 2019/10/11
 **/
public class CanaryRestTemplateIntercptor implements ClientHttpRequestInterceptor {
    /**
     * todo: 透传要在这里设置添加header
     *
     * @param httpRequest
     * @param bytes
     * @param clientHttpRequestExecution
     * @return
     * @throws IOException
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        CanaryTrackContext.setServiceName(httpRequest.getURI().getHost());
        return clientHttpRequestExecution.execute(httpRequest, bytes);
    }
}
