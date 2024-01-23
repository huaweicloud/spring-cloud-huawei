/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.servicecomb.discovery.check;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.common.event.EventManager;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceInstanceRegistrationEvent;

public class RegistryHealthIndicator implements HealthIndicator {

    private boolean isSuccess = false;

    private static final String REGISTRATION_NOT_READY = "registration not ready";

    public RegistryHealthIndicator() {
        EventManager.register(this);
    }

    @Override
    public Health health() {
        if (isSuccess) {
            return Health.up().build();
        }
        return Health.down().withDetail("Error Message", REGISTRATION_NOT_READY).build();
    }

    @Subscribe
    public void registryListener(MicroserviceInstanceRegistrationEvent event) {
            this.isSuccess = event.isSuccess();
    }
}