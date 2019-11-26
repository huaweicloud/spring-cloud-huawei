package com.huaweicloud.common.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Author wangqijun
 * @Date 15:38 2019-11-06
 **/
public class MD5UtilTest {

  @Test
  public void encrypt() {
    String computeValue = "dasfjkl;sdjfkldsafjodsiu29-w0483290-48230-8idfsopafjdls;afkd;safkd;safdsaf";
    Assert.assertEquals(MD5Util.encrypt(computeValue), MD5Util.encrypt(computeValue));
    String changeValue = "dasfjkl;sdjfkldsafjodsiu29-w0483290-48230-8idfsopafjdls;afkd;safkd;safdsa5";
    Assert.assertNotEquals(MD5Util.encrypt(computeValue), MD5Util.encrypt(changeValue));
  }
}