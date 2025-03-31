/*

 * Copyright (C) 2020-2025 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.governance.adapters.feign.options;

import static feign.Util.checkNotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Stream;

import feign.InvocationHandlerFactory.MethodHandler;
import feign.Request;
import feign.Request.Options;
import feign.Target;

@SuppressWarnings("rawtypes")
public class FeignMethodRequestOptionsHandler implements InvocationHandler {
  private final Map<Method, MethodHandler> dispatch;

  public FeignMethodRequestOptionsHandler(Target target, Map<Method, MethodHandler> dispatch) {
    this.dispatch = checkNotNull(dispatch, "dispatch for %s", target);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object[] refactorArgs = restructureRequestArgs(method, args);
    return dispatch.get(method).invoke(refactorArgs);
  }

  private Object[] restructureRequestArgs(Method method, Object[] args) {
    FeignRequestOptions requestOptions = method.getAnnotation(FeignRequestOptions.class);

    // if method have no FeignRequestOptions annotation, or args have options, return origin args
    if (requestOptions == null || findArgsOptions(args) != null) {
      return args;
    }
    Request.Options options = new Options(requestOptions.connectTimeout(), requestOptions.connectTimeoutUnit(),
        requestOptions.readTimeout(), requestOptions.readTimeoutUnit(), requestOptions.followRedirects());
    if (args == null || args.length == 0) {
      return new Object[]{options};
    }
    Object[] argsWithOptions = new Object[args.length + 1];
    System.arraycopy(args, 0, argsWithOptions, 0, args.length);
    argsWithOptions[args.length] = options;
    return argsWithOptions;
  }

  Request.Options findArgsOptions(Object[] argv) {
    if (argv == null || argv.length == 0) {
      return null;
    }
    return Stream.of(argv)
        .filter(Options.class::isInstance)
        .map(Options.class::cast)
        .findFirst()
        .orElse(null);
  }
}
