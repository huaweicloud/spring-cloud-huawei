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
import com.huaweicloud.governance.authentication.webflux.WebFluxRSAProviderAuthManager;

public class WebFluxRSAProviderAuthManagerTest {
  private Environment environment = Mockito.mock(Environment.class);

  private WebFluxRSAProviderAuthManager manager
      = new WebFluxRSAProviderAuthManager(null, environment, null);

  @Test
  public void testCheckUriWhitelistPrefixMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("*/whitelist");
    Assertions.assertFalse(manager.isRequiredAuth("/api/check/whitelist"));
  }

  @Test
  public void testCheckUriExcludePrefixMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_EXCLUDE, String.class, ""))
        .thenReturn("*/whitelist");
    Assertions.assertFalse(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriIncludePrefixMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_INCLUDE, String.class, ""))
        .thenReturn("*/whitelist");
    Assertions.assertTrue(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriWhitelistPrefixSlashMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("/*/whitelist");
    Assertions.assertFalse(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriExcludePrefixSlashMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_EXCLUDE, String.class, ""))
        .thenReturn("/*/whitelist");
    Assertions.assertFalse(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriIncludePrefixSlashMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_INCLUDE, String.class, ""))
        .thenReturn("/*/whitelist");
    Assertions.assertTrue(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriWhitelistPrefixNotMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("*/whitelist");
    Assertions.assertTrue(manager.isRequiredAuth("/api/check/query"));
    clearEnv();
  }

  @Test
  public void testCheckUriExcludePrefixNotMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_EXCLUDE, String.class, ""))
        .thenReturn("*/whitelist");
    Assertions.assertTrue(manager.isRequiredAuth("/api/check/query"));
    clearEnv();
  }

  @Test
  public void testCheckUriIncludePrefixNotMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_INCLUDE, String.class, ""))
        .thenReturn("*/whitelist");
    Assertions.assertFalse(manager.isRequiredAuth("/api/check/query"));
    clearEnv();
  }

  @Test
  public void testCheckUriWhitelistSuffixMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("/api/*");
    Assertions.assertFalse(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriExcludeSuffixMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_EXCLUDE, String.class, ""))
        .thenReturn("/api/*");
    Assertions.assertFalse(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriIncludeSuffixMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_INCLUDE, String.class, ""))
        .thenReturn("/api/*");
    Assertions.assertTrue(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriWhitelistSuffixNotMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("/apg/*");
    Assertions.assertTrue(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriExcludeSuffixNotMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_EXCLUDE, String.class, ""))
        .thenReturn("/apg/*");
    Assertions.assertTrue(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriIncludeSuffixNotMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_INCLUDE, String.class, ""))
        .thenReturn("/apg/*");
    Assertions.assertFalse(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriWhitelistEqualMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("/api/check/whitelist");
    Assertions.assertFalse(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriExcludeEqualMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_EXCLUDE, String.class, ""))
        .thenReturn("/api/check/whitelist");
    Assertions.assertFalse(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriIncludeEqualMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_INCLUDE, String.class, ""))
        .thenReturn("/api/check/whitelist");
    Assertions.assertTrue(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriWhitelistEqualNotMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("/api/check/white");
    Assertions.assertTrue(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriExcludeEqualNotMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_EXCLUDE, String.class, ""))
        .thenReturn("/api/check/white");
    Assertions.assertTrue(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriIncludeEqualNotMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_INCLUDE, String.class, ""))
        .thenReturn("/api/check/white");
    Assertions.assertFalse(manager.isRequiredAuth("/api/check/whitelist"));
    clearEnv();
  }

  @Test
  public void testCheckUriAllNotSet() {
    Assertions.assertTrue(manager.isRequiredAuth("/api/check/whitelist"));
  }

  /**
   * include and exclude both math url, no auth
   */
  @Test
  public void testCheckUriBothMatch() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_INCLUDE, String.class, ""))
        .thenReturn("/api/check/white");
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_EXCLUDE, String.class, ""))
        .thenReturn("/api/check/white");
    Assertions.assertFalse(manager.isRequiredAuth("/api/check/white"));
    clearEnv();
  }

  private void clearEnv() {
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_INCLUDE, String.class, ""))
        .thenReturn("");
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_EXCLUDE, String.class, ""))
        .thenReturn("");
    Mockito.when(environment.getProperty(GovernanceConst.AUTH_API_PATH_WHITELIST, String.class, ""))
        .thenReturn("");
  }
}
