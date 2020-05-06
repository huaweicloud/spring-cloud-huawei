package com.huaweicloud.servicecomb.discovery.ribbon;

import com.huaweicloud.servicecomb.discovery.event.ServerListRefreshEvent;
import com.huaweicloud.servicecomb.discovery.event.ServiceCombEventBus;
import com.netflix.loadbalancer.PollingServerListUpdater;
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
public class ServiceCombServerListUpdater extends PollingServerListUpdater {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombServerListUpdater.class);

  private final AtomicBoolean isActive = new AtomicBoolean(false);

  private final AtomicLong lastUpdated = new AtomicLong(System.currentTimeMillis());

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
    super.start(updateAction);
    if (isActive.compareAndSet(false, true)) {
      eventBus.register(event -> {
        if (!(event instanceof ServerListRefreshEvent)) {
          return;
        }
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
    }
  }

  @Override
  public void stop() {
    super.stop();
    if (isActive.compareAndSet(true, false)) {
      // stop listening
      refreshExecutor.shutdown();
    } else {
      LOGGER.info("Not active, no-op");
    }
  }

  @Override
  public String getLastUpdate() {
    Date pullTime = new Date(super.getLastUpdate());
    Date watchTime = new Date(lastUpdated.get());
    return pullTime.after(watchTime) ? pullTime.toString() : watchTime.toString();
  }

  @Override
  public long getDurationSinceLastUpdateMs() {
    return super.getDurationSinceLastUpdateMs();
  }

  @Override
  public int getNumberMissedCycles() {
    return super.getNumberMissedCycles();
  }

  @Override
  public int getCoreThreads() {
    return super.getCoreThreads();
  }

}