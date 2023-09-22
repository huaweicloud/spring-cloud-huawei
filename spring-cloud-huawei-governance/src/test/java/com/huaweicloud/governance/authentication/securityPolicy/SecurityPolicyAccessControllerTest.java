/*

 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.huaweicloud.governance.authentication.AuthRequestExtractor;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.authentication.securityPolicy.SecurityPolicyProperties.Action;
import com.huaweicloud.governance.authentication.securityPolicy.SecurityPolicyProperties.ConfigurationItem;

public class SecurityPolicyAccessControllerTest {
  private final AuthenticationAdapter authenticationAdapter = Mockito.mock(AuthenticationAdapter.class);

  private SecurityPolicyProperties securityPolicyProperties = new SecurityPolicyProperties();
  private AuthRequestExtractor extractor = new AuthRequestExtractor();

  @BeforeEach
  public void setUp() {
    extractor.setMethod("GET");
    extractor.setServiceName("order");
  }

  @Test
  public void testAllowPermissiveMatch() throws Exception {
    extractor.setApiPath("/checkTokenSecurityAllow");
    Assertions.assertTrue(getAllowAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testDenyPermissiveMatch() throws Exception {
    extractor.setApiPath("/checkTokenSecurityDeny");
    Assertions.assertTrue(getDenyAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testAllowPermissiveNotMatch() throws Exception {
    extractor.setApiPath("/checkToken");
    Assertions.assertTrue(getAllowAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testDenyPermissiveNotMatch() throws Exception {
    extractor.setApiPath("/checkTokenSecurity");
    Assertions.assertTrue(getDenyAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testAllowEnforcingMatch() throws Exception {
    extractor.setApiPath("/checkTokenSecurityAllow");
    Assertions.assertTrue(getAllowAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testDenyEnforcingMatch() throws Exception {
    extractor.setApiPath("/checkTokenSecurityDeny");
    Assertions.assertFalse(getDenyAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testAllowEnforcingNotMatch() throws Exception {
    extractor.setApiPath("/checkToken");
    Assertions.assertFalse(getAllowAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testDenyEnforcingNotMatch() throws Exception {
    extractor.setApiPath("/checkToken");
    Assertions.assertTrue(getDenyAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testPermissiveBothMatch() throws Exception {
    extractor.setApiPath("/checkTokenSecurityBoth");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testPermissiveBothNotMatch() throws Exception {
    extractor.setApiPath("/checkToken");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testPermissiveAllowMatchDenyNotMatch() throws Exception {
    extractor.setApiPath("/checkTokenSecurityAllow");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testPermissiveAllowNotMatchDenyMatch() throws Exception {
    extractor.setApiPath("/checkTokenSecurityDeny");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed(extractor));
  }

  @Test
  public void testEnforcingBothMatch() throws Exception {
    extractor.setApiPath("/checkTokenSecurityBoth");
    Assertions.assertFalse(getBothAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testEnforcingBothNotMatch() throws Exception {
    extractor.setApiPath("/checkToken");
    Assertions.assertFalse(getBothAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testEnforcingAllowMatchDenyNotMatch() throws Exception {
    extractor.setApiPath("/checkTokenSecurityAllow");
    Assertions.assertTrue(getBothAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testEnforcingAllowNotMatchDenyMatch() throws Exception {
    extractor.setApiPath("/checkTokenSecurityDeny");
    Assertions.assertFalse(getBothAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testUriPrefixMatch() throws Exception {
    extractor.setApiPath("/checkTokenPre/security/allow");
    Assertions.assertTrue(getAllowAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testUriPrefixNotMatch() throws Exception {
    extractor.setApiPath("/checkTokenPer/security/allow");
    Assertions.assertFalse(getAllowAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testUriSuffixMatch() throws Exception {
    extractor.setApiPath("/checkTokenPer/security/checkTokenSuf");
    Assertions.assertTrue(getAllowAccessController("enforcing")
        .isAllowed(extractor));
  }

  @Test
  public void testUriSuffixNotMatch() throws Exception {
    extractor.setApiPath("/checkTokenPer/security/checkTokenSfu");
    Assertions.assertFalse(getAllowAccessController("enforcing")
        .isAllowed(extractor));
  }

  private SecurityPolicyAccessController getAllowAccessController(String mode) {
    Action action = new Action();
    action.setAllow(buildAllow());
    securityPolicyProperties.setAction(action);
    securityPolicyProperties.setMode(mode);
    return new SecurityPolicyAccessController(authenticationAdapter, securityPolicyProperties);
  }

  private SecurityPolicyAccessController getDenyAccessController(String mode) {
    Action action = new Action();
    action.setDeny(buildDeny());
    securityPolicyProperties.setAction(action);
    securityPolicyProperties.setMode(mode);
    return new SecurityPolicyAccessController(authenticationAdapter, securityPolicyProperties);
  }

  private SecurityPolicyAccessController getBothAccessController(String mode) {
    Action action = new Action();
    action.setAllow(buildAllow());
    action.setDeny(buildDeny());
    securityPolicyProperties.setAction(action);
    securityPolicyProperties.setMode(mode);
    return new SecurityPolicyAccessController(authenticationAdapter, securityPolicyProperties);
  }

  private List<ConfigurationItem> buildDeny() {
    List<ConfigurationItem> list = new ArrayList<>();
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setConsumer("order");
    configurationItem.setId("1");
    configurationItem.setMethod("GET");
    configurationItem.setUri("/checkTokenSecurityDeny");
    list.add(configurationItem);
    list.add(buildBoth());
    return list;
  }

  private List<ConfigurationItem> buildAllow() {
    List<ConfigurationItem> list = new ArrayList<>();
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setConsumer("order");
    configurationItem.setId("1");
    configurationItem.setMethod("GET");
    configurationItem.setUri("/checkTokenSecurityAllow");
    list.add(configurationItem);
    list.add(buildBoth());
    list.add(buildUriPrefix());
    list.add(buildUriSuffix());
    return list;
  }

  private ConfigurationItem buildBoth() {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setConsumer("order");
    configurationItem.setId("1");
    configurationItem.setMethod("GET");
    configurationItem.setUri("/checkTokenSecurityBoth");
    return configurationItem;
  }

  private ConfigurationItem buildUriPrefix() {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setConsumer("order");
    configurationItem.setId("1");
    configurationItem.setMethod("GET");
    configurationItem.setUri("/checkTokenPre/*");
    return configurationItem;
  }

  private ConfigurationItem buildUriSuffix() {
    ConfigurationItem configurationItem = new ConfigurationItem();
    configurationItem.setConsumer("order");
    configurationItem.setId("1");
    configurationItem.setMethod("GET");
    configurationItem.setUri("*/checkTokenSuf");
    return configurationItem;
  }
}
