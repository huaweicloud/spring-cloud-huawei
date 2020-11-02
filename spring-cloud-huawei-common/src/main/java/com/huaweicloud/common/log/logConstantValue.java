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
package com.huaweicloud.common.log;

/**
 * @Author zyl
 * @Date 2020/10/28
 **/
public class logConstantValue {

  /**
   * log level
   */
  public static final String LOG_LEVEL_DEBUG = "DEBUG";
  public static final String LOG_LEVEL_INFO = "INFO";
  public static final String LOG_LEVEL_WARN = "WARN";
  public static final String LOG_LEVEL_ERROR = "ERROR";

  /**
   * module name
   */
  public static final String MODULE_CONFIG = "ConfigServer";
  public static final String MODULE_CIRCUITBREAKER = "CircuitBreaker";
  public static final String MODULE_RATELIMITER = "RateLimiter";
  public static final String MODULE_ROUTER = "Router";
  public static final String MODULE_LB = "LB";
  public static final String MODULE_DISCOVERY = "Discovery";
  public static final String MODULE_AUTH = "Auth";
  public static final String MODULE_GOVERNANCE = "governance";
  public static final String MODULE_DTM = "dtm";

  /**
   * event
   */
  public static final String EVENT_OPEN = "open";
  public static final String EVENT_CLOSE = "close";
  public static final String EVENT_ERROR = "error";
  public static final String EVENT_TIMEOUT = "timeout";
  public static final String EVENT_TRYCLOSE = "tryClose";
  public static final String EVENT_CREATE = "create";
  public static final String EVENT_DELETE = "delete";
  public static final String EVENT_UPDATE = "update";
  public static final String EVENT_RETRY = "retry";
  public static final String EVENT_POLL = "poll";
  public static final String EVENT_WATCH = "watch";
  public static final String EVENT_HEARTBEAT = "heartbeat";
  public static final String EVENT_SUCCESS = "success";
  public static final String EVENT_REGISTER = "register";
  public static final String EVENT_REQUEST = "request";

  /**
   * system
   */
  public static final String SYSTEM_SERVICECOMB = "serviceComb";
}
