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

package com.huaweicloud.governance.authentication;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSAProviderTokenManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSAProviderTokenManager.class);

  private final List<AccessController> accessControllers;

  public RSAProviderTokenManager(List<AccessController> accessControllers) {
    this.accessControllers = accessControllers;
  }

  public void valid(String token, Map<String, String> requestMap) throws Exception {
    try {
      for (AccessController accessController : accessControllers) {
        accessController.valid(token, requestMap);
      }
    }catch(Exception e){
      LOGGER.error("verify error", e);
      throw e;
    }
  }
}
