package org.springframework.cloud.canary.client.ribbon;

import com.netflix.loadbalancer.Server;
import org.springframework.cloud.canary.core.distribute.AbstractCanaryDistributer;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstance;
import org.springframework.cloud.servicecomb.discovery.discovery.MicroserviceCache;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class CanaryServerDistributer extends AbstractCanaryDistributer<Server, MicroserviceInstance> {
    public CanaryServerDistributer() {
        init(server -> MicroserviceCache.getMicroserviceIns(server.getMetaInfo().getInstanceId()),
                MicroserviceInstance::getVersion,
                MicroserviceInstance::getServiceName,
                MicroserviceInstance::getProperties);
    }
}
