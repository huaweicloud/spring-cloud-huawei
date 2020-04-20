package com.huaweicloud.servicecomb.discovery.ribbon;

import com.huaweicloud.servicecomb.discovery.event.ServiceCombEventBus;
import com.netflix.loadbalancer.ServerListUpdater;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author GuoYl123
 * @Date 2020/4/20
 */
public class ServiceCombServerListUpdater implements ServerListUpdater {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombServerListUpdater.class);

  private final AtomicBoolean isActive = new AtomicBoolean(false);

  private final AtomicLong lastUpdated = new AtomicLong(System.currentTimeMillis());

  private UpdateAction updateAction;

  private ScheduledExecutorService refreshExecutor =
      Executors.newScheduledThreadPool(
          1,
          (r) -> {
            Thread thread = new Thread(r);
            thread.setName("com.huaweicloud.servercenter.watch.refresh");
            thread.setDaemon(true);
            return thread;
          });

  @Autowired
  private ServiceCombEventBus eventBus;

  @Override
  public void start(UpdateAction updateAction) {
    if (isActive.compareAndSet(false, true)) {
      this.updateAction = updateAction;
      eventBus.register(event -> {
        if (!refreshExecutor.isShutdown()) {
          try {
            refreshExecutor.submit(
                () -> {
                  try {
                    updateAction.doUpdate();
                    lastUpdated.set(System.currentTimeMillis());
                  } catch (Exception e) {
                    LOGGER.warn("failed to update serverList", e);
                  }
                });
          } catch (Exception e) {
            LOGGER
                .error("error submitting watch task to executor", e);
          }
        }
      });
      eventBus.trigger();
    }
  }

  @Override
  public void stop() {
    if (isActive.compareAndSet(true, false)) {
      // stop listening
      refreshExecutor.shutdown();
    } else {
      LOGGER.info("Not active, no-op");
    }
  }

  @Override
  public String getLastUpdate() {
    return new Date(lastUpdated.get()).toString();
  }

  @Override
  public long getDurationSinceLastUpdateMs() {
    return 0;
  }

  @Override
  public int getNumberMissedCycles() {
    return 0;
  }

  @Override
  public int getCoreThreads() {
    return 1;
  }

}