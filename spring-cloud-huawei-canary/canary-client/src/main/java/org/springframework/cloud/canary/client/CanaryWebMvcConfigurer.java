package org.springframework.cloud.canary.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.canary.client.track.CanaryHandlerInterceptor;
import org.springframework.cloud.canary.client.track.CanaryTrackInfo;
import org.springframework.cloud.canary.client.track.CanaryTrackThreadLocalInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CanaryHandlerInterceptor()).addPathPatterns("/**");
    }
}
