package com.huaweicloud.samples;

import com.huaweicloud.servicecomb.discovery.registry.ServiceCombAutoServiceRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

public class ServiceCombListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private ServiceCombAutoServiceRegistration registration;

    @Override
    @Deprecated
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String serverPort = event.getApplicationContext().getEnvironment().getProperty("server.port");
        registration.setPort(Integer.valueOf(serverPort));
        registration.start();
    }
}
