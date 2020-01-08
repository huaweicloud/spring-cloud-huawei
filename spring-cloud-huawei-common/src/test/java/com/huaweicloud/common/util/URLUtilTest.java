package com.huaweicloud.common.util;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Author wangqijun
 * @Date 17:41 2019-08-07
 **/
public class URLUtilTest {

  @Test
  public void transform() {
    Assert.assertEquals(URLUtil.transform("rest://aa.com"), "http://aa.com");
    Assert.assertEquals(URLUtil.transform("rest://aa.com/?sslEnabled=true"), "https://aa.com/");
    Assert.assertNull(URLUtil.transform(null));
  }

  @Test
  public void splitIpPort() {
    String[] res = URLUtil.splitIpPort("http://0.0.0.0:3000/?end=udu");
    Assert.assertEquals(res[0], "0.0.0.0");
    Assert.assertEquals(res[1], "3000");
  }

  @Test
  public void isEquals() {
    String url1 = "http://127.0.0.1:3030";
    String url2 = "http://127.0.0.1:3030";
    Assert.assertTrue(URLUtil.isEquals(url1, url2));
    String url3 = "http://www.ddd.com";
    String url4 = "http://www.ddd.com";
    Assert.assertTrue(URLUtil.isEquals(url3, url4));
    String url5 = "http://127.0.0.1:3030";
    String url6 = "http://127.0.0.1:3030/";
    Assert.assertTrue(URLUtil.isEquals(url5, url6));
    Assert.assertFalse(URLUtil.isEquals(null, url6));
    Assert.assertFalse(URLUtil.isEquals(null, null));
  }
}
