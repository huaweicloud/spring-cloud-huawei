package org.springframework.cloud.servicecomb.discovery.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

import org.springframework.cloud.servicecomb.discovery.client.ServiceCombClient;
import org.springframework.cloud.servicecomb.discovery.client.model.HeartbeatRequest;
import org.springframework.cloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

/**
 * @Author wangqijun
 * @Date 16:33 2019-08-06
 **/
public class HeartbeatScheduler {

  private final TaskScheduler scheduler = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());

  private final Map<String, ScheduledFuture> heartbeatRequestMap = new ConcurrentHashMap<>();

  private ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  private ServiceCombClient serviceCombClient;

  public HeartbeatScheduler(ServiceCombDiscoveryProperties serviceCombDiscoveryProperties,
      ServiceCombClient serviceCombClient) {
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
    this.serviceCombClient = serviceCombClient;
  }

  public void add(String instanceId, String serviceId) {
    if (!serviceCombDiscoveryProperties.isHealthCheck()) {
      return;
    }
    HeartbeatRequest heartbeatRequest = new HeartbeatRequest(serviceId, instanceId);
    ScheduledFuture currentTask = this.scheduler
        .scheduleWithFixedDelay(new HeartbeatTask(heartbeatRequest, serviceCombClient),
            serviceCombDiscoveryProperties.getHealthCheckInterval() * 1000);
    ScheduledFuture preScheduled = heartbeatRequestMap.put(instanceId, currentTask);
    if (null != preScheduled) {
      preScheduled.cancel(true);
    }
  }

  public void remove(String instanceId) {
    ScheduledFuture scheduled = heartbeatRequestMap.get(instanceId);
    if (null != scheduled) {
      scheduled.cancel(true);
    }
    heartbeatRequestMap.remove(instanceId);
  }
}
