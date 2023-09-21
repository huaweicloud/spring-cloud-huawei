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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.authentication.securityPolicy.SecurityPolicyProperties.Action;
import com.huaweicloud.governance.authentication.securityPolicy.SecurityPolicyProperties.ConfigurationItem;

public class SecurityPolicyAccessControllerTest {
  private final AuthenticationAdapter authenticationAdapter = Mockito.mock(AuthenticationAdapter.class);

  private SecurityPolicyProperties securityPolicyProperties = new SecurityPolicyProperties();

  @Test
  public void testAllowPermissiveMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenSecurityAllow");
    requestMap.put("method", "GET");
    requestMap.put("serviceId", "order");
    Assertions.assertTrue(getAllowAccessController("permissive")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testDenyPermissiveMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenSecurityDeny");
    requestMap.put("method", "GET");
    Assertions.assertTrue(getDenyAccessController("permissive")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testAllowPermissiveNotMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkToken");
    requestMap.put("method", "GET");
    Assertions.assertTrue(getAllowAccessController("permissive")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testDenyPermissiveNotMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenSecurity");
    requestMap.put("method", "GET");
    Assertions.assertTrue(getDenyAccessController("permissive")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testAllowEnforcingMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenSecurityAllow");
    requestMap.put("method", "GET");
    Assertions.assertTrue(getAllowAccessController("enforcing")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testDenyEnforcingMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenSecurityDeny");
    requestMap.put("method", "GET");
    Assertions.assertFalse(getDenyAccessController("enforcing")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testAllowEnforcingNotMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkToken");
    requestMap.put("method", "GET");
    Assertions.assertFalse(getAllowAccessController("enforcing")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testDenyEnforcingNotMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkToken");
    requestMap.put("method", "GET");
    Assertions.assertTrue(getDenyAccessController("enforcing")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testPermissiveBothMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenSecurityBoth");
    requestMap.put("method", "GET");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testPermissiveBothNotMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkToken");
    requestMap.put("method", "GET");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testPermissiveAllowMatchDenyNotMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenSecurityAllow");
    requestMap.put("method", "GET");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testPermissiveAllowNotMatchDenyMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenSecurityDeny");
    requestMap.put("method", "GET");
    Assertions.assertTrue(getBothAccessController("permissive")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testEnforcingBothMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenSecurityBoth");
    requestMap.put("method", "GET");
    Assertions.assertFalse(getBothAccessController("enforcing")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testEnforcingBothNotMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkToken");
    requestMap.put("method", "GET");
    Assertions.assertFalse(getBothAccessController("enforcing")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testEnforcingAllowMatchDenyNotMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenSecurityAllow");
    requestMap.put("method", "GET");
    Assertions.assertTrue(getBothAccessController("enforcing")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testEnforcingAllowNotMatchDenyMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenSecurityDeny");
    requestMap.put("method", "GET");
    Assertions.assertFalse(getBothAccessController("enforcing")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testUriPrefixMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenPre/security/allow");
    requestMap.put("method", "GET");
    Assertions.assertTrue(getAllowAccessController("enforcing")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testUriPrefixNotMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenPer/security/allow");
    requestMap.put("method", "GET");
    Assertions.assertFalse(getAllowAccessController("enforcing")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testUriSuffixMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenPer/security/checkTokenSuf");
    requestMap.put("method", "GET");
    Assertions.assertTrue(getAllowAccessController("enforcing")
        .isAllowed("order", requestMap));
  }

  @Test
  public void testUriSuffixNotMatch() {
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("uri", "/checkTokenPer/security/checkTokenSfu");
    requestMap.put("method", "GET");
    Assertions.assertFalse(getAllowAccessController("enforcing")
        .isAllowed("order", requestMap));
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
