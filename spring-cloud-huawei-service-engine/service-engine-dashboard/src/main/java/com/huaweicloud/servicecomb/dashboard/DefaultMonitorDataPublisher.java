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

package com.huaweicloud.servicecomb.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.config.RequestConfig;
import org.apache.servicecomb.dashboard.client.DashboardAddressManager;
import org.apache.servicecomb.dashboard.client.DashboardClient;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;

import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombSSLProperties;
import com.huaweicloud.service.engine.common.configration.dynamic.DashboardProperties;
import com.huaweicloud.service.engine.common.transport.TransportUtils;
import com.huaweicloud.common.util.URLUtil;
import com.huaweicloud.servicecomb.dashboard.model.MonitorDataProvider;
import com.huaweicloud.servicecomb.dashboard.model.MonitorDataPublisher;

public class DefaultMonitorDataPublisher implements MonitorDataPublisher {
  private final ServiceCombSSLProperties serviceCombSSLProperties;

  private final DashboardProperties dashboardProperties;

  private final List<AuthHeaderProvider> authHeaderProviders;

  private DashboardClient dashboardClient;

  public DefaultMonitorDataPublisher(ServiceCombSSLProperties serviceCombSSLProperties,
      DashboardProperties dashboardProperties, List<AuthHeaderProvider> authHeaderProviders) {
    this.serviceCombSSLProperties = serviceCombSSLProperties;
    this.dashboardProperties = dashboardProperties;
    this.authHeaderProviders = authHeaderProviders;
  }

  @Override
  public void init() {
    DashboardAddressManager addressManager = createDashboardAddressManager();

    RequestConfig.Builder requestBuilder = HttpTransportFactory.defaultRequestConfig();
    requestBuilder.setConnectionRequestTimeout(1000);
    requestBuilder.setSocketTimeout(10000);

    HttpTransport httpTransport = createHttpTransport(addressManager, requestBuilder.build());

    dashboardClient = new DashboardClient(addressManager, httpTransport);
  }

  private DashboardAddressManager createDashboardAddressManager() {
    List<String> addresses = URLUtil.dealMultiUrl(dashboardProperties.getAddress());

    if (addresses.isEmpty()) {
      throw new IllegalStateException("dashboard address is not configured.");
    }

    return new DashboardAddressManager(addresses, EventManager.getEventBus());
  }

  private HttpTransport createHttpTransport(DashboardAddressManager addressManager, RequestConfig requestConfig) {
    return HttpTransportFactory
        .createHttpTransport(
            TransportUtils
                .createSSLProperties(addressManager.sslEnabled(), serviceCombSSLProperties),
            getRequestAuthHeaderProvider(authHeaderProviders), requestConfig);
  }

  private static RequestAuthHeaderProvider getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
    return signRequest -> {
      Map<String, String> headers = new HashMap<>();
      authHeaderProviders.forEach(provider -> headers.putAll(provider.authHeaders()));
      return headers;
    };
  }

  @Override
  public void publish(MonitorDataProvider provider) {
    dashboardClient.sendData(provider.getURL(), provider.getData());
  }
}
