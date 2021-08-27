package com.huaweicloud.sample;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

public class GovernanceControllerIT {

  RestTemplate template = new RestTemplate();

  @Test
  public void testGatewayRateLimiting() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(Config.GATEWAY_URL + "/gateway/sayHello", String.class);
              if (!"Hello world!".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!"429 Too Many Requests: [rate limited]".equals(e.getMessage())) {
                notExpectedFailed.set(true);
              }
              expectedFailed.set(true);
            }
            latch.countDown();
          }
        }.start();
      }
      Thread.sleep(100);
    }
    latch.await(20, TimeUnit.SECONDS);
    Assert.assertEquals(true, expectedFailed.get());
    Assert.assertEquals(false, notExpectedFailed.get());
  }

}
