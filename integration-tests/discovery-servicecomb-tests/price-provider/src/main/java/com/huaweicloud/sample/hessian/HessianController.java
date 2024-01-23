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

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hessian")
public class HessianController implements HessianService {
  @Override
  public Base base(Base b) {
    return b;
  }

  @Override
  public Generic<Base> generic(Generic<Base> b) {
    return b;
  }

  @Override
  public NonSerializableModel nonSerializableModel(NonSerializableModel b) {
    return b;
  }

  @Override
  public NonSerializableModel[] nonSerializableModelArray(NonSerializableModel[] b) {
    return b;
  }
}
