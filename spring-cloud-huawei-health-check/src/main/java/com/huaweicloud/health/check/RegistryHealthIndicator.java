package com.huaweicloud.health.check;

import com.google.common.eventbus.Subscribe;
import org.springframework.stereotype.Component;
import org.springframework.boot.actuate.health.*;
import com.huaweicloud.common.event.EventManager;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceRegistrationEvent;

@Component
public class RegistryHealthIndicator implements HealthIndicator {

    private boolean isSuccess = false;

    private String Detail = null;

    public RegistryHealthIndicator() {
        EventManager.register(this);
    }

    @Override
    public Health health() {
        if (isSuccess) {
            return Health.up().build();
        }
        return Health.down().withDetail("Error Message", Detail).build();
    }

    @Subscribe
    public void registryListener(MicroserviceRegistrationEvent event) {
        if (event.isSuccess()) {
            this.isSuccess = true;
            this.Detail = "registry was successful";
        } else {
            this.Detail = "registry has failed";
        }
    }
}