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

package com.huaweicloud.zookeeper.discovery;

import org.apache.curator.framework.CuratorFrameworkFactory;

/**
 * Beans that implement this interface will be used to extends CuratorFramework
 */
@FunctionalInterface
public interface ServiceCuratorFrameworkCustomizer {

  /**
   * Customize the {@link CuratorFrameworkFactory.Builder}.
   * @param builder instance of the builder that you can further customize.
   */
  void customize(CuratorFrameworkFactory.Builder builder);
}
