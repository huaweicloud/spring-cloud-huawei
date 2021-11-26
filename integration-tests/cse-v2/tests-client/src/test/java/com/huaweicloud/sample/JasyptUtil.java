/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huaweicloud.sample;

import org.jasypt.util.text.BasicTextEncryptor;

public class JasyptUtil {
  //此处设置为配置项jasypt.encryptor.password的密码
  public static String salt = "GXXX6";

  //加密方法
  public static String demoEncrypt(String value) {
    BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    textEncryptor.setPassword(salt);
    return textEncryptor.encrypt(value);
  }

  //测试解密是否正常
  public static String demoDecrypt(String value) {
    BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    textEncryptor.setPassword(salt);
    return textEncryptor.decrypt(value);
  }

  public static void main(String[] args) {
    String username = demoEncrypt("123456Lbc@");
    System.out.println(username);
    username = demoDecrypt(username);
    System.out.println(username);
  }
}
