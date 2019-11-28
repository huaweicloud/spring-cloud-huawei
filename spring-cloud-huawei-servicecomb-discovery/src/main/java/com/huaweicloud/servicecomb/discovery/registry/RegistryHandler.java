package com.huaweicloud.servicecomb.discovery.registry;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huaweicloud.common.util.NetUtil;
import com.huaweicloud.servicecomb.discovery.client.model.Framework;
import com.huaweicloud.servicecomb.discovery.client.model.HealthCheck;
import com.huaweicloud.servicecomb.discovery.client.model.HealthCheckMode;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstance;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceStatus;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

/**
 * @Author wangqijun
 * @Date 15:18 2019-08-08
 **/
public class RegistryHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryHandler.class);


  public static MicroserviceInstance buildMicroServiceInstances(String serviceID, Microservice microservice,
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties) {
    MicroserviceInstance microserviceInstance = buildInstance(serviceID, serviceCombDiscoveryProperties);
    List<MicroserviceInstance> instances = new ArrayList<>();
    instances.add(microserviceInstance);
    microservice.setInstances(instances);
    microservice.setStatus(MicroserviceStatus.UP);
    return microserviceInstance;
  }


  private static MicroserviceInstance buildInstance(String serviceID,
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties) {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setServiceId(serviceID);
    microserviceInstance.setHostName(NetUtil.getLocalHost());
    List<String> endPoints = new ArrayList<>();
    String address = NetUtils.getHostAddress();
    endPoints.add("http://" + address + ":" + serviceCombDiscoveryProperties.getPort());
    microserviceInstance.setEndpoints(endPoints);
    HealthCheck healthCheck = new HealthCheck();
    healthCheck.setMode(HealthCheckMode.PLATFORM);
    healthCheck.setInterval(serviceCombDiscoveryProperties.getHealthCheckInterval());
    healthCheck.setTimes(3);
    microserviceInstance.setHealthCheck(healthCheck);
    String currTime = String.valueOf(System.currentTimeMillis());
    microserviceInstance.setTimestamp(currTime);
    microserviceInstance.setModTimestamp(currTime);
    microserviceInstance.setVersion(serviceCombDiscoveryProperties.getVersion());
    return microserviceInstance;
  }

  public static Microservice buildMicroservice(ServiceCombRegistration registration) {
    Microservice microservice = new Microservice();
    microservice.setAppId(registration.getAppName());
    microservice.setServiceName(registration.getServiceId());
    microservice.setVersion(registration.getVersion());
    microservice.setFramework(new Framework());
    return microservice;
  }
}
