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
package com.huaweicloud.governance.client;

import java.util.ArrayList;
import java.util.List;

import com.huaweicloud.common.ribbon.RibbonServerFilter;
import com.huaweicloud.governance.client.track.RequestTrackContext;
import com.netflix.loadbalancer.Server;

public class GovRibbonServerFilter implements RibbonServerFilter {

  /**
   * @param list
   * @return
   */
  @Override
  public List<Server> filter(List<Server> list) {
    List<Server> copyList = new ArrayList<>(list);
    if (RequestTrackContext.getServerExcluder().isEnabled()) {
      copyList.removeAll(RequestTrackContext.getServerExcluder().getIgnoreServers());
      if (!copyList.isEmpty()) {
        return copyList;
      }
    }
    return list;
  }

  @Override
  public int order() {
    return 0;
  }
}
