package org.springframework.cloud.canary.client.feign;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author GuoYl123
 * @Date 2019/10/23
 **/
@Configuration
public class CanaryFeignConfiguration {
    @Bean
    public Client getFeignClient(Client feignClient){
        return new CanaryFeignClient(feignClient);
    }
}
