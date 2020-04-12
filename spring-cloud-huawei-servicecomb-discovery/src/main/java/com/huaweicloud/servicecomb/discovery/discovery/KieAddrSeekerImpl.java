package com.huaweicloud.servicecomb.discovery.discovery;

import com.huaweicloud.common.discovery.KieAddrSeeker;
import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.util.StringUtils;

/**
 * @Author GuoYl123
 * @Date 2020/3/3
 **/
public class KieAddrSeekerImpl implements KieAddrSeeker {

  private static final Logger LOGGER = LoggerFactory.getLogger(KieAddrSeeker.class);

  @Autowired
  private ServiceCombClient serviceCombClient;

  @Override
  public String getKieAddr() {
    Microservice microservice = new Microservice();
    microservice.setServiceName("servicecomb-kie");
    List<ServiceInstance> instanceList;
    try {
      instanceList = serviceCombClient.getInstances(microservice, null);
    } catch (ServiceCombException e) {
      LOGGER.warn("get kie instances failed.", e);
      return null;
    }
    StringBuilder url = new StringBuilder("");
    for (ServiceInstance ins : instanceList) {
      if (!StringUtils.isEmpty(url.toString())) {
        url.append(",");
      }
      //todo: ssl
      url.append("http://").append(ins.getHost()).append(":").append(ins.getPort());
    }
    return url.toString();
  }
}
