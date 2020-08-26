package com.huaweicloud.servicecomb.discovery.discovery;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @Author GuoYl123
 * @Date 2020/8/25
 **/
public class ServiceCombWatch implements ApplicationEventPublisherAware, SmartLifecycle {

  private ServiceCombDiscoveryProperties discoveryProperties;

  private final AtomicBoolean isActive = new AtomicBoolean(false);

  private ApplicationEventPublisher publisher;

  private ScheduledFuture<?> watchFuture;

  private final TaskScheduler taskScheduler = new ConcurrentTaskScheduler(
      Executors.newSingleThreadScheduledExecutor());

  private final AtomicLong index = new AtomicLong(0);

  public ServiceCombWatch(
      ServiceCombDiscoveryProperties discoveryProperties) {
    this.discoveryProperties = discoveryProperties;
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.publisher = applicationEventPublisher;
  }

  @Override
  public void start() {
    if (this.isActive.compareAndSet(false, true)) {
      this.watchFuture = this.taskScheduler.scheduleWithFixedDelay(
          () -> {
            this.publisher.publishEvent(new HeartbeatEvent(this, index.getAndIncrement()));
          }, discoveryProperties.getRefreshInterval());
    }
  }

  @Override
  public void stop() {
    if (this.isActive.compareAndSet(true, false) && this.watchFuture != null) {
      ((ThreadPoolTaskScheduler) this.taskScheduler).shutdown();

      this.watchFuture.cancel(true);
    }
  }

  @Override
  public boolean isRunning() {
    return this.isActive.get();
  }
}
