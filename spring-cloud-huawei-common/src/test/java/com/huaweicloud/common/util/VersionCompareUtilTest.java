package com.huaweicloud.common.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Author GuoYl123
 * @Date 2020/2/6
 **/
public class VersionCompareUtilTest {

  @Test
  public void testVersion() {
    Assert.assertTrue(VersionCompareUtil.compareVersion("0.0.1", "0.0.0") > 0);
    Assert.assertEquals(0, VersionCompareUtil.compareVersion("0.0.0", "0.0.0"));
    Assert.assertTrue(VersionCompareUtil.compareVersion("0.0.0", "0.0.1") < 0);
    Assert.assertTrue(VersionCompareUtil.compareVersion("0.0.0", "0.0.0.0") < 0);
    Assert.assertTrue(VersionCompareUtil.compareVersion("0.0.1", "0.0.0.0") > 0);
    Assert.assertTrue(VersionCompareUtil.compareVersion("0.0.1", "0.0.0.0") > 0);
  }
}
