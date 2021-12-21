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

package com.huaweicloud.common.event;

import java.util.Set;

import org.springframework.context.ApplicationEvent;

public class ConfigRefreshEvent extends ApplicationEvent {
  private static final long serialVersionUID = -1L;

  private Set<String> change;

  /**
   * Create a new ApplicationEvent.
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public ConfigRefreshEvent(Object source, Set<String> change) {
    super(source);
    this.change = change;
  }

  public Set<String> getChange() {
    return change;
  }

  public void setChange(Set<String> change) {
    this.change = change;
  }
}


