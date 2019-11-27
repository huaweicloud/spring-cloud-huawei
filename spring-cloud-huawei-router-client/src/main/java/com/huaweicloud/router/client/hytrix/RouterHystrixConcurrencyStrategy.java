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
package com.huaweicloud.router.client.hytrix;

import java.util.Map;
import java.util.concurrent.Callable;

import com.huaweicloud.router.client.track.RouterTrackContext;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;

/**
 * @Author GuoYl123
 * @Date 2019/10/24
 **/
public class RouterHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

  public RouterHystrixConcurrencyStrategy() {
    HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
  }

  @Override
  public <T> Callable<T> wrapCallable(Callable<T> callable) {
    return new RouterAttributeAwareCallable<>(callable, RouterTrackContext.getServiceName(),
        RouterTrackContext
            .getRequestHeader());
  }

  static class RouterAttributeAwareCallable<T> implements Callable<T> {

    private final Callable<T> delegate;

    private final String serviceName;

    private final Map<String, String> requestHeader;

    public RouterAttributeAwareCallable(Callable<T> delegate, String serviceName,
        Map<String, String> requestHeader) {
      this.delegate = delegate;
      this.serviceName = serviceName;
      this.requestHeader = requestHeader;
    }

    @Override
    public T call() throws Exception {
      try {
        RouterTrackContext.setRequestHeader(requestHeader);
        RouterTrackContext.setServiceName(serviceName);
        return delegate.call();
      } finally {
        RouterTrackContext.remove();
      }
    }
  }
}
