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

import org.springframework.boot.context.config.ConfigDataResource;
import org.springframework.boot.context.config.Profiles;

public class NacosConfigDataResource extends ConfigDataResource {
  private final String group;

  private final String dataId;

  private final boolean refreshEnabled;

  private final String preference;

  private final String fileExtension;

  private final boolean optionalEnabled;

  private final Profiles profiles;

  public NacosConfigDataResource(String group, String dataId, boolean refreshEnabled, String preference,
      String fileExtension, boolean optional, Profiles profiles) {
    this.group = group;
    this.dataId = dataId;
    this.refreshEnabled = refreshEnabled;
    this.preference = preference;
    this.fileExtension = fileExtension;
    this.optionalEnabled = optional;
    this.profiles = profiles;
  }

  public String getGroup() {
    return group;
  }

  public String getDataId() {
    return dataId;
  }

  public boolean isRefreshEnabled() {
    return refreshEnabled;
  }

  public String getPreference() {
    return preference;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  public boolean isOptionalEnabled() {
    return this.optionalEnabled;
  }

  public Profiles getProfiles() {
    return profiles;
  }
}
