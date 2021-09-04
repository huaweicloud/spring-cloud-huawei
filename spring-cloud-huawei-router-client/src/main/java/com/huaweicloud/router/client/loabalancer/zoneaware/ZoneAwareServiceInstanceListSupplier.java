/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.router.client.loabalancer.zoneaware;

import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServiceInstance;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.loadbalancer.core.DelegatingServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class ZoneAwareServiceInstanceListSupplier extends DelegatingServiceInstanceListSupplier {

    private ServiceCombRegistration  serviceCombRegistration;

    public ZoneAwareServiceInstanceListSupplier(ServiceInstanceListSupplier delegate, ServiceCombRegistration serviceCombRegistration) {
        super(delegate);
        this.serviceCombRegistration = serviceCombRegistration;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Flux<List<ServiceInstance>> get(Request request) {
        Flux<List<ServiceInstance>> result = getDelegate().get(request);
        return result.map(this::filter);
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return getDelegate().get();
    }

    private List<ServiceInstance> filter(List<ServiceInstance> instances) {
        MicroserviceInstance mySelf = serviceCombRegistration.getMicroserviceInstance();
        List<ServiceInstance> filterInstances =  zoneAwareDiscoveryFilter(mySelf, instances);
        return filterInstances;
    }

    @SuppressWarnings({"all"})
    private List<ServiceInstance> zoneAwareDiscoveryFilter(MicroserviceInstance mySelf, List<ServiceInstance> instances) {
        List<ServiceInstance> regionAndAZMatchList = new ArrayList<>();
        List<ServiceInstance> regionMatchList = new ArrayList<>();
        instances.forEach(serviceInstance -> {
            ServiceCombServiceInstance instance = (ServiceCombServiceInstance) serviceInstance;
            if (regionAndAZMatch(mySelf, instance.getMicroserviceInstance())) {
                regionAndAZMatchList.add(serviceInstance);
            } else if (regionMatch(mySelf, instance.getMicroserviceInstance())) {
                regionMatchList.add(serviceInstance);
            }
        });
        if (!regionAndAZMatchList.isEmpty()) {
            return regionAndAZMatchList;
        }
        if (!regionMatchList.isEmpty()) {
            return regionMatchList;
        }
        return instances;
    }

    private boolean regionAndAZMatch(MicroserviceInstance myself, MicroserviceInstance target) {
        if (myself.getDataCenterInfo() != null && target.getDataCenterInfo() != null) {
            return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion()) &&
                    myself.getDataCenterInfo().getAvailableZone().equals(target.getDataCenterInfo().getAvailableZone());
        }
        return false;
    }

    private boolean regionMatch(MicroserviceInstance myself, MicroserviceInstance target) {
        if (target.getDataCenterInfo() != null) {
            return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion());
        }
        return false;
    }
}
