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

package com.huaweicloud.common.transport;

import java.util.Map;

import org.apache.servicecomb.service.center.client.exception.OperationException;
import org.apache.servicecomb.service.center.client.model.RbacTokenResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RBACRequestAuthHeaderProviderTest {
  private final DiscoveryBootstrapProperties discoveryProperties = Mockito.mock(DiscoveryBootstrapProperties.class);

  private final ServiceCombSSLProperties serviceCombSSLProperties = Mockito.mock(ServiceCombSSLProperties.class);

  private final ServiceCombRBACProperties serviceCombRBACProperties = Mockito.mock(ServiceCombRBACProperties.class);

  @Before
  public void setUp() {
    Mockito.when(serviceCombRBACProperties.getName()).thenReturn("test_name");
    Mockito.when(serviceCombRBACProperties.getPassword()).thenReturn("test_password");
  }

  static class FirstTimeSuccessRBACRequestAuthHeaderProvider extends RBACRequestAuthHeaderProvider {
    public FirstTimeSuccessRBACRequestAuthHeaderProvider(DiscoveryBootstrapProperties discoveryProperties,
        ServiceCombSSLProperties serviceCombSSLProperties,
        ServiceCombRBACProperties serviceCombRBACProperties) {
      super(discoveryProperties, serviceCombSSLProperties, serviceCombRBACProperties);
    }

    protected RbacTokenResponse callCreateHeaders() {
      RbacTokenResponse response = new RbacTokenResponse();
      response.setStatusCode(200);
      response.setToken("test_token");
      return response;
    }
  }

  static class SecondTimeSuccessRBACRequestAuthHeaderProvider extends RBACRequestAuthHeaderProvider {
    private boolean first = true;

    public SecondTimeSuccessRBACRequestAuthHeaderProvider(DiscoveryBootstrapProperties discoveryProperties,
        ServiceCombSSLProperties serviceCombSSLProperties,
        ServiceCombRBACProperties serviceCombRBACProperties) {
      super(discoveryProperties, serviceCombSSLProperties, serviceCombRBACProperties);
    }

    protected RbacTokenResponse callCreateHeaders() {
      if (first) {
        first = false;
        throw new OperationException("query token failed");
      }
      RbacTokenResponse response = new RbacTokenResponse();
      response.setStatusCode(200);
      response.setToken("test_token");
      return response;
    }
  }

  static class SecondTimeFirstNullSuccessRBACRequestAuthHeaderProvider extends RBACRequestAuthHeaderProvider {
    private int count = 0;

    public SecondTimeFirstNullSuccessRBACRequestAuthHeaderProvider(DiscoveryBootstrapProperties discoveryProperties,
        ServiceCombSSLProperties serviceCombSSLProperties,
        ServiceCombRBACProperties serviceCombRBACProperties) {
      super(discoveryProperties, serviceCombSSLProperties, serviceCombRBACProperties);
    }

    protected long refreshTime() {
      return 100;
    }

    protected RbacTokenResponse callCreateHeaders() {
      RbacTokenResponse response = new RbacTokenResponse();
      count++;
      if (count == 1) {
        response.setStatusCode(400);
        return response;
      }
      if (count == 2) {
        response.setStatusCode(200);
        response.setToken("test_token");
        return response;
      }
      response.setStatusCode(200);
      response.setToken("test_token_2");
      return response;
    }
  }

  @Test
  public void testFirstTimeSuccess() {
    RBACRequestAuthHeaderProvider provider = new FirstTimeSuccessRBACRequestAuthHeaderProvider(discoveryProperties,
        serviceCombSSLProperties, serviceCombRBACProperties);
    Map<String, String> result = provider.authHeaders();
    Assert.assertEquals("Bearer test_token", result.get(RBACRequestAuthHeaderProvider.AUTH_HEADER));
    result = provider.authHeaders();
    Assert.assertEquals("Bearer test_token", result.get(RBACRequestAuthHeaderProvider.AUTH_HEADER));
  }

  @Test
  public void testSecondTimeSuccess() {
    RBACRequestAuthHeaderProvider provider = new SecondTimeSuccessRBACRequestAuthHeaderProvider(discoveryProperties,
        serviceCombSSLProperties, serviceCombRBACProperties);
    Map<String, String> result = provider.authHeaders();
    Assert.assertTrue(result.isEmpty());
    result = provider.authHeaders();
    Assert.assertEquals("Bearer test_token", result.get(RBACRequestAuthHeaderProvider.AUTH_HEADER));
    result = provider.authHeaders();
    Assert.assertEquals("Bearer test_token", result.get(RBACRequestAuthHeaderProvider.AUTH_HEADER));
  }

  @Test
  public void testSecondTimeSuccessFirstNull() throws Exception {
    RBACRequestAuthHeaderProvider provider = new SecondTimeFirstNullSuccessRBACRequestAuthHeaderProvider(
        discoveryProperties,
        serviceCombSSLProperties, serviceCombRBACProperties);
    Map<String, String> result = provider.authHeaders();
    Assert.assertTrue(result.isEmpty());
    result = provider.authHeaders();
    Assert.assertEquals("Bearer test_token", result.get(RBACRequestAuthHeaderProvider.AUTH_HEADER));
    result = provider.authHeaders();
    Assert.assertEquals("Bearer test_token", result.get(RBACRequestAuthHeaderProvider.AUTH_HEADER));
    for (int i = 0; i < 20; i++) {
      Thread.sleep(100);
      result = provider.authHeaders();// wait a while
      if ("Bearer test_token_2".equals(result)) {
        break;
      }
    }
    result = provider.authHeaders();
    Assert.assertEquals("Bearer test_token_2", result.get(RBACRequestAuthHeaderProvider.AUTH_HEADER));
  }
}
