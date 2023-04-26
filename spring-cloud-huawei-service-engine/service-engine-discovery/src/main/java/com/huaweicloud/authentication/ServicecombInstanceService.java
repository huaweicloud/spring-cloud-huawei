/*
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.authentication;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.huaweicloud.governance.authentication.Const;
import com.huaweicloud.governance.authentication.MicroserviceInstanceService;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ServicecombInstanceService implements MicroserviceInstanceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServicecombInstanceService.class);

    private final ServiceCombRegistration registration;

    private final ServiceCenterClient client;

    public ServicecombInstanceService(ServiceCombRegistration registration, ServiceCenterClient client) {
        this.registration = registration;
        this.client = client;
    }

    @Override
    public void setPublickey(String publicKeyEncoded) {
        registration.getMicroserviceInstance().getProperties().put(Const.INSTANCE_PUBKEY_PRO,
                publicKeyEncoded);
    }

    @Override
    public String getInstanceId() {
        return registration.getMicroserviceInstance().getInstanceId();
    }

    @Override
    public String getServiceId() {
        return registration.getMicroservice().getServiceId();
    }

    @Override
    public String getPublicKeyFromInstance(String instanceId, String serviceId) {
        MicroserviceInstance instances = getOrCreate(serviceId, instanceId);
        if (instances != null) {
            return instances.getProperties().get(Const.INSTANCE_PUBKEY_PRO);
        } else {
            LOGGER.error("not instance found {}-{}, maybe attack", instanceId, serviceId);
            return "";
        }
    }

    @Override
    public Map<String, String> getProperties(String serviceId, String instanceId, String propertyName) {
        Microservice microservice = getMicroservice(serviceId);
        try {
            Object fieldValue = new PropertyDescriptor(propertyName, Microservice.class).getReadMethod().invoke(microservice);
            if (fieldValue.getClass().getName().equals(String.class.getName())) {
                Map<String, String> property = new HashMap<>();
                property.put(propertyName, (String) fieldValue);
                return property;
            }
        } catch (Exception e) {
            LOGGER.warn("can't find property name: {} in microservice field.", propertyName);
        }
        return microservice.getProperties();
    }

    private Microservice getMicroservice(String serviceId) {
        try {
            return microservices.get(serviceId, () -> {
                Microservice microservice = client.getMicroserviceByServiceId(serviceId);
                if (microservice == null) {
                    throw new IllegalArgumentException("service id not exists.");
                }
                return microservice;
            });
        } catch (ExecutionException | UncheckedExecutionException e) {
            LOGGER.error("get microservice from cache failed, {}, {}", serviceId, e.getMessage());
            return null;
        }
    }

    public MicroserviceInstance getOrCreate(String serviceId, String instanceId) {
        try {
            String key = String.format("%s@%s", serviceId, instanceId);
            return instances.get(key, () -> {
                MicroserviceInstance instance = client.getMicroserviceInstance(serviceId, instanceId);
                if (instance == null) {
                    throw new IllegalArgumentException("instance id not exists.");
                }
                return instance;
            });
        } catch (ExecutionException | UncheckedExecutionException e) {
            LOGGER.error("get microservice instance from cache failed, {}, {}",
                    String.format("%s@%s", serviceId, instanceId),
                    e.getMessage());
            return null;
        }
    }

    private static final Cache<String, Microservice> microservices = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();

    private static final Cache<String, MicroserviceInstance> instances = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();
}