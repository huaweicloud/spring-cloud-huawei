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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.authentication.securityPolicy.SecurityPolicyProperties.Action;
import com.huaweicloud.governance.authentication.securityPolicy.SecurityPolicyProperties.ConfigurationItem;

public class SecurityPolicyAccessControllerTest {
  private final AuthenticationAdapter authenticationAdapter = Mockito.mock(AuthenticationAdapter.class);

  private SecurityPolicyProperties securityPolicyProperties = new SecurityPolicyProperties();

  private Map<String, String> requestMap = new HashMap<>();

  private final Environment environment = Mockito.mock(Environment.class);

  @BeforeEach
  public void setUp() {
    requestMap.put("serviceName", "order");
    requestMap.put("method", "GET");
  }

  @Test
  public void testAllowPermissiveMatch() throws Exception {
    requestMap.put("uri", "/checkTokenSecurityAllow");
    Assertions.assertTrue(getAllowAccessController("permissive")
        .isAllowed(requestMap));
  }

  @Test
  public void testDenyPermissiveMatch() throws Exception {
    requestMap.put("uri", "/checkTokenSecurityDeny");
    Assertions.assertTrue(getDenyAccessController("permissive")
        .isAllowed(requestMap));
  }

  @Test
  public void testAllowPermissiveNotMatch() throws Exception {
    requestMap.put("uri", "/checkToken");
    Assertions.assertTrue(getAllowAccessController("permissive")
        .isAllowed(requestMap));
  }

  @Test
  public void testDenyPermissiveNotMatch() throws Exception {
    requestMap.put("uri", "/checkTokenSecurity");
    Assertions.assertTrue(getDenyAccessController("permissive")
        .isAllowed(requestMap));
  }

  @Test
  public void testAllowEnforcingMatch() throws Exception {
    requestMap.put("uri", "/checkTokenSecurityAllow");
    Assertions.assertTrue(getAllowAccessController("enforcing")
        .isAllowed(requestMap));
  }

  @Test
  public void testDenyEnforcingMatch() throws Exception {
    requestMap.put("uri", "/checkTokenSecurityDeny");
    Assertions.assertFalse(getDenyAccessController("enforcing")
        .isAllowed(requestMap));
  }

  @Test
  public void testAllowEnforcingNotMatch() throws Exception {
    requestMap.put("uri", "/checkToken");
    Assertions.assertFalse(getAllowAccessController("enforcing")
        .isAllowed(requestMap));
  }

  @Test
  public void testDenyEnforcingNotMatch() throws Exception {
    requestMap.put("uri", "/checkToken");
    Assertions.assertTrue(getDenyAccessController("enforcing")
        .isAllowed(requestMap));
  }

  @Test
  public void testPermissiveBothMatch() throws Exception {
    requestMap.put("uri", "/checkTokenSecurityBoth");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed(requestMap));
  }

  @Test
  public void testPermissiveBothNotMatch() throws Exception {
    requestMap.put("uri", "/checkToken");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed(requestMap));
  }

  @Test
  public void testPermissiveAllowMatchDenyNotMatch() throws Exception {
    requestMap.put("uri", "/checkTokenSecurityAllow");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed(requestMap));
  }

  @Test
  public void testPermissiveAllowNotMatchDenyMatch() throws Exception {
    requestMap.put("uri", "/checkTokenSecurityDeny");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed(requestMap));
  }

  @Test
  public void testEnforcingBothMatch() throws Exception {
    requestMap.put("uri", "/checkTokenSecurityBoth");
    Assertions.assertFalse(getBothAccessController("enforcing")
        .isAllowed(requestMap));
  }

  @Test
  public void testEnforcingBothNotMatch() throws Exception {
    requestMap.put("uri", "/checkToken");
    Assertions.assertFalse(getBothAccessController("enforcing")
        .isAllowed(requestMap));
  }

  @Test
  public void testEnforcingAllowMatchDenyNotMatch() throws Exception {
    requestMap.put("uri", "/checkTokenSecurityAllow");
    Assertions.assertTrue(getBothAccessController("enforcing")
        .isAllowed( requestMap));
  }

  @Test
  public void testEnforcingAllowNotMatchDenyMatch() throws Exception {
    requestMap.put("uri", "/checkTokenSecurityDeny");
    Assertions.assertFalse(getBothAccessController("enforcing")
        .isAllowed(requestMap));
  }

  @Test
  public void testUriPrefixMatch() throws Exception {
    requestMap.put("uri", "/checkTokenPre/security/allow");
    Assertions.assertTrue(getAllowAccessController("enforcing")
        .isAllowed(requestMap));
  }

  @Test
  public void testUriPrefixNotMatch() throws Exception {
    requestMap.put("uri", "/checkTokenPer/security/allow");
    Assertions.assertFalse(getAllowAccessController("enforcing")
        .isAllowed(requestMap));
  }

  @Test
  public void testUriSuffixMatch() throws Exception {
    requestMap.put("uri", "/checkTokenPer/security/checkTokenSuf");
    Assertions.assertTrue(getAllowAccessController("enforcing")
        .isAllowed(requestMap));
  }

  @Test
  public void testUriSuffixNotMatch() throws Exception {
    requestMap.put("uri", "/checkTokenPer/security/checkTokenSfu");
    Assertions.assertFalse(getAllowAccessController("enforcing")
        .isAllowed(requestMap));
  }

  private SecurityPolicyAccessController getAllowAccessController(String mode) {
    Action action = new Action();
    action.setAllow(buildAllow());
    securityPolicyProperties.setAction(action);
    securityPolicyProperties.setMode(mode);
    return new SecurityPolicyAccessController(authenticationAdapter, securityPolicyProperties, environment);
  }

  private SecurityPolicyAccessController getDenyAccessController(String mode) {
    Action action = new Action();
    action.setDeny(buildDeny());
    securityPolicyProperties.setAction(action);
    securityPolicyProperties.setMode(mode);
    return new SecurityPolicyAccessController(authenticationAdapter, securityPolicyProperties, environment);
  }

  private SecurityPolicyAccessController getBothAccessController(String mode) {
    Action action = new Action();
    action.setAllow(buildAllow());
    action.setDeny(buildDeny());
    securityPolicyProperties.setAction(action);
    securityPolicyProperties.setMode(mode);
    return new SecurityPolicyAccessController(authenticationAdapter, securityPolicyProperties, environment);
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
