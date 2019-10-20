package org.springframework.cloud.canary.client;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.canary.client.track.CanaryTrackContext;
import org.springframework.cloud.canary.client.track.CanaryTrackInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class CanaryInitializer  implements ApplicationContextAware, InitializingBean {
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() {
        CanaryTrackContext.setCanaryTrackInfo(applicationContext.getBean(CanaryTrackInfo.class));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
