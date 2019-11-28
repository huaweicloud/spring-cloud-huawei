package com.huaweicloud.config.client;

import static org.junit.Assert.assertEquals;

import com.huaweicloud.config.ConfigWatch;
import org.junit.Test;

/**
 * @Author wangqijun
 * @Date 20:42 2019-10-27
 **/
public class ConfigWatchTest {


  @Test
  public void isRunning() {
    ConfigWatch configWatch = new ConfigWatch();
    assertEquals(configWatch.isRunning(), false);
  }

  @Test
  public void isAutoStartup() {
    ConfigWatch configWatch = new ConfigWatch();
    assertEquals(configWatch.isAutoStartup(), true);
  }
}