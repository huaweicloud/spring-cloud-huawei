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
package com.huaweicloud.dtm;

import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DtmConstants {
  private static final Logger LOGGER = LoggerFactory.getLogger(DtmConstants.class);

  public static final String DTM_CONTEXT = "DTM_CONTEXT";

  private static final String DTM_CONTEXT_DEFAULT_CLASS_NAME = "com.huawei.middleware.dtm.client.context.DTMContext";

  private static final String DTM_EXPORT_METHOD_NAME = "getContextData";

  private static final String DTM_IMPORT_METHOD_NAME = "setContextData";

  public static Method DTM_CONTEXT_EX_METHOD;

  public static Method DTM_CONTEXT_IM_METHOD;

  static {
    try {
      Class<?> clazz = Class.forName(DTM_CONTEXT_DEFAULT_CLASS_NAME);
      DTM_CONTEXT_EX_METHOD = clazz.getMethod(DTM_EXPORT_METHOD_NAME);
      DTM_CONTEXT_IM_METHOD = clazz.getMethod(DTM_IMPORT_METHOD_NAME, Map.class);
    } catch (Throwable e) {
      // ignore just warn
      LOGGER.error("Failed to init method {} {} {}, import dtm client libraries to use dtm",
          DTM_CONTEXT_DEFAULT_CLASS_NAME, DTM_EXPORT_METHOD_NAME,
          DTM_IMPORT_METHOD_NAME);
    }
  }
}
