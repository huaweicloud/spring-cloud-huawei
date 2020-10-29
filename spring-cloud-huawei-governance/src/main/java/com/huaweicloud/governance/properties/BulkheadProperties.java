package com.huaweicloud.governance.properties;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.huaweicloud.governance.policy.BulkheadPolicy;

@Component
@ConfigurationProperties("servicecomb")
public class BulkheadProperties implements GovProperties<BulkheadPolicy> {

  Map<String, String> bulkhead;

  @Autowired
  SerializeCache<BulkheadPolicy> cache;

  public Map<String, String> getBulkhead() {
    return bulkhead;
  }

  public void setBulkhead(Map<String, String> bulkhead) {
    this.bulkhead = bulkhead;
  }

  @Override
  public Map<String, BulkheadPolicy> covert() {
    return cache.get(bulkhead, BulkheadPolicy.class);
  }
}
