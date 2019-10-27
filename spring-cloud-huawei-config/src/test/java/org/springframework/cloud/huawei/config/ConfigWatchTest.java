package org.springframework.cloud.huawei.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @Author wangqijun
 * @Date 20:42 2019-10-27
 **/
public class ConfigWatchTest {


  @Test
  public void isRunning() {
    ConfigWatch configWatch = new ConfigWatch(null, null, null);
    assertEquals(configWatch.isRunning(), false);
  }

  @Test
  public void isAutoStartup() {
    ConfigWatch configWatch = new ConfigWatch(null, null, null);
    assertEquals(configWatch.isAutoStartup(), true);
  }
}