package org.springframework.cloud.canary.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.canary.client.rest.CanaryRestTemplateIntercptor;
import org.springframework.cloud.canary.client.track.CanaryHandlerInterceptor;
import org.springframework.cloud.canary.client.track.CanaryTrackInfo;
import org.springframework.cloud.canary.client.track.CanaryTrackThreadLocalInfo;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
@Configuration
public class CanaryWebMvcConfigurer implements WebMvcConfigurer {
    @Bean
    @ConditionalOnMissingBean
    public CanaryTrackInfo localStorageTrackInfo() {
        return new CanaryTrackThreadLocalInfo();
        //todo: 这个要看下 后面要适配hytrix线程池模式下的threadLocal跨线程传递问题
        //return new LocalStorageLifeCycle.NoOpLocalStorageLifeCycle();
    }

    @Bean
    public CanaryInitializer canaryInitializer() {
        return new CanaryInitializer();
    }


//    @Bean
//    public RequestInterceptor requestInterceptor() {
//        return new CanaryFeignIntercptor();
//    }

    @Bean
    public CanaryRestTemplateIntercptor grayClientHttpRequestIntercptor(
            @Autowired(required = false) @LoadBalanced List<RestTemplate> restTemplates) {
        CanaryRestTemplateIntercptor intercptor = new CanaryRestTemplateIntercptor();
        if (restTemplates != null) {
            restTemplates.forEach(restTemplate -> restTemplate.getInterceptors().add(intercptor));
        }
        return intercptor;
    }

//    @Bean
//    public CanaryTrackFilter setFilter(){
//        return new CanaryTrackFilter();
//    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CanaryHandlerInterceptor()).addPathPatterns("/**");
    }
}
