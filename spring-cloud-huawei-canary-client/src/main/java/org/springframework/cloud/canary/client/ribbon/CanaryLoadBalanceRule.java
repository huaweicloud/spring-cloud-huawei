package org.springframework.cloud.canary.client.ribbon;

import com.google.common.base.Optional;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.canary.client.track.CanaryTrackContext;
import org.springframework.cloud.canary.core.CanaryFilter;

import java.util.List;

/**
 * @Author GuoYl123
 * @Date 2019/10/11
 **/
public class CanaryLoadBalanceRule extends ZoneAvoidanceRule {

    private static final String SERVICE_NAME = "spring.application.name";

    @Override
    public Server choose(Object key) {
        CanaryServerDistributer distributer = new CanaryServerDistributer();
        String currentServiceName = DynamicPropertyFactory.getInstance()
                .getStringProperty(SERVICE_NAME, null).get();
        if (StringUtils.isEmpty(currentServiceName)) {
            throw new RuntimeException("error when read the service name");
        }
        List<Server> serverList = CanaryFilter
                .getFilteredListOfServers(getLoadBalancer().getAllServers(),
                        CanaryTrackContext.getServiceName(),
                        currentServiceName,
                        CanaryTrackContext.getRequestHeader(),
                        distributer);
        Optional<Server> server = super.getPredicate().chooseRoundRobinAfterFiltering(serverList, key);
        if (server.isPresent()) {
            return server.get();
        } else {
            return null;
        }
    }
}
