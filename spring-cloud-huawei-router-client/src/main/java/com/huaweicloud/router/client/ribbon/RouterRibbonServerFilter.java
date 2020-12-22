package com.huaweicloud.router.client.ribbon;

import java.util.List;

import com.huaweicloud.common.ribbon.RibbonServerFilter;
import com.huaweicloud.router.client.track.RouterTrackContext;
import com.huaweicloud.router.core.RouterFilter;
import com.netflix.loadbalancer.Server;

public class RouterRibbonServerFilter implements RibbonServerFilter {

  RouterDistributor distributor = new RouterDistributor();

  @Override
  public List<Server> filter(List<Server> list) {
    List<Server> serverList = RouterFilter
        .getFilteredListOfServers(list,
            RouterTrackContext.getServiceName(),
            RouterTrackContext.getRequestHeader(),
            distributor);
    return serverList;
  }

  @Override
  public int getOrder() {
    return 1;
  }
}