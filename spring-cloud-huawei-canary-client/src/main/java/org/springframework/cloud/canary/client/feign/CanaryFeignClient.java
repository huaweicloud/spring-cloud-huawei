package org.springframework.cloud.canary.client.feign;

import feign.Client;
import feign.Request;
import feign.Response;
import org.springframework.cloud.canary.client.track.CanaryTrackContext;

import java.io.IOException;
import java.net.URI;

/**
 * @Author GuoYl123
 * @Date 2019/10/22
 **/
public class CanaryFeignClient implements Client {
    private Client client;

    public CanaryFeignClient(Client client) {
        this.client = client;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        URI uri = URI.create(request.url());
        String host =uri.getHost();
        CanaryTrackContext.setServiceName(host);
        return client.execute(request,options);
    }
}
