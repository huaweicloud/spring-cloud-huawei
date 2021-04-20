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

package com.huaweicloud.common.util;

import java.util.List;

public final class DefaultCipher implements Cipher {
  public static final String CIPHER_NAME = "default";

  private static final DefaultCipher INSTANCE = new DefaultCipher();

  public static DefaultCipher getInstance() {
    return INSTANCE;
  }

  private DefaultCipher() {
  }

  @Override
  public String name() {
    return CIPHER_NAME;
  }

  @Override
  public char[] decrypt(char[] encrypted) {
    return encrypted;
  }

  public static Cipher findCipher(List<Cipher> ciphers, String akskCustomCipher) {
    if (CIPHER_NAME.equals(akskCustomCipher)) {
      return DefaultCipher.getInstance();
    }

    if (ciphers == null) {
      throw new IllegalArgumentException("failed to find cipher named " + akskCustomCipher);
    }

    return ciphers.stream().filter(c -> c.name().equals(akskCustomCipher)).findFirst()
        .orElseThrow(() -> new IllegalArgumentException("failed to find cipher named " + akskCustomCipher));
  }
}

