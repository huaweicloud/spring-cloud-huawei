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

package com.huaweicloud.chaincontext.hystrix;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.huaweicloud.chaincontext.ChainContextHolder;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariable;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableLifecycle;
import com.netflix.hystrix.strategy.properties.HystrixProperty;

public class ChainContextConcurrencyStrategy extends HystrixConcurrencyStrategy {

  private HystrixConcurrencyStrategy existingConcurrencyStrategy;

  public ChainContextConcurrencyStrategy(HystrixConcurrencyStrategy existingConcurrencyStrategy) {
    this.existingConcurrencyStrategy = existingConcurrencyStrategy;
  }

  @Override
  public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
    return existingConcurrencyStrategy != null ? existingConcurrencyStrategy.getBlockingQueue(maxQueueSize)
        : super.getBlockingQueue(maxQueueSize);
  }

  @Override
  public <T> HystrixRequestVariable<T> getRequestVariable(HystrixRequestVariableLifecycle<T> rv) {
    return existingConcurrencyStrategy != null ? existingConcurrencyStrategy.getRequestVariable(rv)
        : super.getRequestVariable(rv);
  }

  @Override
  public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey, HystrixProperty<Integer> corePoolSize,
      HystrixProperty<Integer> maximumPoolSize, HystrixProperty<Integer> keepAliveTime, TimeUnit unit,
      BlockingQueue<Runnable> workQueue) {
    return existingConcurrencyStrategy != null
        ? existingConcurrencyStrategy.getThreadPool(threadPoolKey, corePoolSize, maximumPoolSize, keepAliveTime, unit,
        workQueue)
        : super.getThreadPool(threadPoolKey, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
  }

  @Override
  public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
      HystrixThreadPoolProperties threadPoolProperties) {
    return existingConcurrencyStrategy != null
        ? existingConcurrencyStrategy.getThreadPool(threadPoolKey, threadPoolProperties)
        : super.getThreadPool(threadPoolKey, threadPoolProperties);
  }

  @Override
  public <T> Callable<T> wrapCallable(Callable<T> callable) {
    return existingConcurrencyStrategy != null
        ? existingConcurrencyStrategy
        .wrapCallable(new DelegatingChainContextCallable<T>(callable, ChainContextHolder.getCurrentContext()))
        : super.wrapCallable(new DelegatingChainContextCallable<T>(callable, ChainContextHolder.getCurrentContext()));
  }
}
