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

package com.huaweicloud.nacos.config.configdata;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;

import com.huaweicloud.nacos.config.NacosConfigConst;
import com.huaweicloud.nacos.config.NacosConfigProperties;
import com.huaweicloud.nacos.config.manager.NacosConfigServiceMasterManager;
import com.huaweicloud.nacos.config.manager.NacosConfigServiceStandbyManager;

public class NacosConfigDataLocationResolver implements ConfigDataLocationResolver<NacosConfigDataResource>, Ordered {
  public static final String CONFIG_PREFIX = "nacos:";

  private static final String GROUP = "group";

  private static final String REFRESH_ENABLED = "refreshEnabled";

  private static final String PREFERENCE = "preference";

  @Override
  public int getOrder() {
    return -1;
  }

  @Override
  public boolean isResolvable(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
    if (!location.hasPrefix(CONFIG_PREFIX)) {
      return false;
    }
    return context.getBinder()
        .bind(NacosConfigProperties.PREFIX + ".enabled", boolean.class)
        .orElse(true);
  }

  @Override
  public List<NacosConfigDataResource> resolve(ConfigDataLocationResolverContext context, ConfigDataLocation location)
      throws ConfigDataLocationNotFoundException, ConfigDataResourceNotFoundException {
    return Collections.emptyList();
  }

  @Override
  public List<NacosConfigDataResource> resolveProfileSpecific(ConfigDataLocationResolverContext context,
      ConfigDataLocation location, Profiles profiles) throws ConfigDataLocationNotFoundException {
    NacosConfigProperties properties = getProperties(context);
    ConfigurableBootstrapContext bootstrapContext = context.getBootstrapContext();
    bootstrapContext.registerIfAbsent(NacosConfigProperties.class, InstanceSupplier.of(properties));
    registerConfigServiceManager(properties, bootstrapContext);
    return loadConfigDataResources(location, profiles, properties);
  }

  private List<NacosConfigDataResource> loadConfigDataResources(ConfigDataLocation location, Profiles profiles,
      NacosConfigProperties properties) {
    URI uri = buildQueryConfigUri(location, properties);
    String dataId = getDataIdByUri(uri);
    if (StringUtils.isEmpty(dataId)) {
      throw new IllegalArgumentException("dataId has no set or error format!");
    }
    List<NacosConfigDataResource> configDataResources = new ArrayList<>();
    NacosConfigDataResource source = new NacosConfigDataResource(getGroup(uri, properties), dataId,
        getRefreshEnabled(uri, properties), getQueryMap(uri).get(PREFERENCE), getFileExtension(dataId, properties),
        location.isOptional(), profiles);
    configDataResources.add(source);
    return configDataResources;
  }

  private URI buildQueryConfigUri(ConfigDataLocation location, NacosConfigProperties properties) {
    String path = location.getNonPrefixedValue(CONFIG_PREFIX);
    path = StringUtils.isBlank(path) ? "/" : path.startsWith("/") ? path : "/" + path;
    String uri = properties.getServerAddr() + path;
    return buildUri(uri);
  }

  private URI buildUri(String uri) {
    String realUri = uri.startsWith("http") ? uri : "http://" + uri;
    try {
      return new URI(realUri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("build uri error, URI: " + uri, e);
    }
  }

  private void registerConfigServiceManager(NacosConfigProperties properties, ConfigurableBootstrapContext bootstrapContext) {
    if (!bootstrapContext.isRegistered(NacosConfigServiceMasterManager.class)) {
      bootstrapContext.register(NacosConfigServiceMasterManager.class,
          InstanceSupplier.of(new NacosConfigServiceMasterManager(properties)));
    }
    if (!bootstrapContext.isRegistered(NacosConfigServiceStandbyManager.class)) {
      bootstrapContext.register(NacosConfigServiceStandbyManager.class,
          InstanceSupplier.of(new NacosConfigServiceStandbyManager(properties)));
    }
  }

  private NacosConfigProperties getProperties(ConfigDataLocationResolverContext context) {
    NacosConfigProperties nacosConfigProperties;
    if (context.getBootstrapContext().isRegistered(NacosConfigProperties.class)) {
      nacosConfigProperties = context.getBootstrapContext().get(NacosConfigProperties.class);
    } else {
      Binder binder = context.getBinder();
      BindHandler bindHandler = context.getBootstrapContext().getOrElse(BindHandler.class, null);
      nacosConfigProperties = binder
          .bind("spring.cloud.nacos", Bindable.of(NacosConfigProperties.class), bindHandler)
          .map(properties -> binder
              .bind(NacosConfigProperties.PREFIX, Bindable.of(NacosConfigProperties.class), bindHandler)
                  .orElse(properties))
          .orElseGet(() -> binder
              .bind(NacosConfigProperties.PREFIX, Bindable.of(NacosConfigProperties.class), bindHandler)
              .orElseGet(NacosConfigProperties::new));
    }
    return nacosConfigProperties;
  }

  private String getDataIdByUri(URI uri) {
    if (uri.getPath() == null || uri.getPath().length() <= 1) {
      return StringUtils.EMPTY;
    }
    String[] parts = uri.getPath().substring(1).split("/");
    if (parts.length != 1) {
      throw new IllegalArgumentException("illegal dataId!");
    }
    return parts[0];
  }

  private String getGroup(URI uri, NacosConfigProperties properties) {
    Map<String, String> queryMap = getQueryMap(uri);
    return queryMap.containsKey(GROUP) ? queryMap.get(GROUP) : properties.getGroup();
  }

  private Map<String, String> getQueryMap(URI uri) {
    if (StringUtils.isBlank(uri.getQuery())) {
      return Collections.emptyMap();
    }
    Map<String, String> queryMap = new HashMap<>();
    for (String entry : uri.getQuery().split("&")) {
      String[] queryItem = entry.split("=");
      if (queryItem.length == 2) {
        queryMap.put(queryItem[0], queryItem[1]);
      }
    }
    return queryMap;
  }

  private String getFileExtension(String dataId, NacosConfigProperties properties) {
    if (dataId != null && dataId.contains(NacosConfigConst.DOT)) {
      return dataId.substring(dataId.lastIndexOf(NacosConfigConst.DOT) + 1);
    }
    return properties.getFileExtension();
  }

  private boolean getRefreshEnabled(URI uri, NacosConfigProperties properties) {
    Map<String, String> queryMap = getQueryMap(uri);
    return queryMap.containsKey(REFRESH_ENABLED) ?
        Boolean.parseBoolean(queryMap.get(REFRESH_ENABLED)) : properties.isRefreshEnabled();
  }
}
