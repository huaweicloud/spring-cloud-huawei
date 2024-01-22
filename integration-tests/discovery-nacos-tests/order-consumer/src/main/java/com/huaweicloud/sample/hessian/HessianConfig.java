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

package com.huaweicloud.sample.hessian;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HessianConfig {
  @FeignClient(name = "price", contextId = "hessianService", path = "/hessian")
  public interface HessianServiceExt extends HessianService {
  }

  @FeignClient(name = "price", contextId = "childService", path = "/hessian/child")
  public interface IChildServiceExt extends IChildService {
  }

  @FeignClient(name = "price", contextId = "childServiceBase", path = "/hessian/baseChild")
  public interface IChildBaseServiceExt extends IChildBaseService {
  }
}
