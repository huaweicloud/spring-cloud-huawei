package com.huaweicloud.servicecomb.discovery.discovery;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * @Author GuoYl123
 * @Date 2020/8/18
 **/
public class ServiceCombReactiveDiscoveryClient implements ReactiveDiscoveryClient {

  private DiscoveryClient discoveryClient;

  public ServiceCombReactiveDiscoveryClient(DiscoveryClient serviceCombDiscoveryClient) {
    this.discoveryClient = serviceCombDiscoveryClient;
  }

  @Override
  public String description() {
    return "this is servicecomb reactive implement";
  }

  @Override
  public Flux<ServiceInstance> getInstances(String serviceId) {
    return Flux.defer(() -> Flux.fromIterable(discoveryClient.getInstances(serviceId)))
        .subscribeOn(Schedulers.boundedElastic());
  }


  @Override
  public Flux<String> getServices() {
    return Flux.defer(() -> Flux.fromIterable(discoveryClient.getServices()))
        .subscribeOn(Schedulers.boundedElastic());
  }
}
