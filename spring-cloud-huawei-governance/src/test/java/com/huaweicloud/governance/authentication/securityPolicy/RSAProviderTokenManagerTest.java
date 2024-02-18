/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.governance.authentication.securityPolicy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.huaweicloud.governance.GovernanceConst;
import com.huaweicloud.governance.authentication.RSAProviderTokenManager;

public class RSAProviderTokenManagerTest {
  private Environment environment = Mockito.mock(Environment.class);

  private RSAProviderTokenManager manager
      = new RSAProviderTokenManager(null, environment, null);

  @Test
  public void testCheckUriWhitelistPrefixMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("*/whitelist");
    Assertions.assertTrue(manager.checkUriWhitelist("/api/check/whitelist"));
  }

  @Test
  public void testCheckUriWhitelistPrefixSlashMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("/*/whitelist");
    Assertions.assertTrue(manager.checkUriWhitelist("/api/check/whitelist"));
  }
  @Test
  public void testCheckUriWhitelistPrefixNotMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("*/whitelist");
    Assertions.assertFalse(manager.checkUriWhitelist("/api/check/query"));
  }

  @Test
  public void testCheckUriWhitelistSuffixMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("/api/*");
    Assertions.assertTrue(manager.checkUriWhitelist("/api/check/whitelist"));
  }

  @Test
  public void testCheckUriWhitelistSuffixNotMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("/apg/*");
    Assertions.assertFalse(manager.checkUriWhitelist("/api/check/whitelist"));
  }

  @Test
  public void testCheckUriWhitelistEqualMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("/api/check/whitelist");
    Assertions.assertTrue(manager.checkUriWhitelist("/api/check/whitelist"));
  }

  @Test
  public void testCheckUriWhitelistEqualNotMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("/api/check/white");
    Assertions.assertFalse(manager.checkUriWhitelist("/api/check/whitelist"));
  }
}
