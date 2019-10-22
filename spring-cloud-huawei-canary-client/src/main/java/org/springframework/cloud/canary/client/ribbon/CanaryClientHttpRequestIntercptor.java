package org.springframework.cloud.canary.client.ribbon;

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
public class CanaryClientHttpRequestIntercptor implements ClientHttpRequestInterceptor {
    /**
     * todo: feign
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
        CanaryTrackContext.setRequestHeader(httpRequest.getHeaders().toSingleValueMap());
        return clientHttpRequestExecution.execute(httpRequest, bytes);
    }
}
