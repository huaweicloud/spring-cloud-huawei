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

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface HessianService {
  @PostMapping(path = "base", consumes = "x-application/hessian2",
      produces = "x-application/hessian2")
  Base base(@RequestBody Base b);

  @PostMapping(path = "generic", consumes = "x-application/hessian2",
      produces = "x-application/hessian2")
  Generic<Base> generic(@RequestBody Generic<Base> b);

  @PostMapping(path = "nonSerializableModel", consumes = "x-application/hessian2",
      produces = "x-application/hessian2")
  NonSerializableModel nonSerializableModel(@RequestBody NonSerializableModel b);

  @PostMapping(path = "nonSerializableModelArray", consumes = "x-application/hessian2",
      produces = "x-application/hessian2")
  NonSerializableModel[] nonSerializableModelArray(@RequestBody NonSerializableModel[] b);
}
