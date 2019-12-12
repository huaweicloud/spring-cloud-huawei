package com.huaweicloud.servicecomb.discovery.registry;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author GuoYl123
 * @Date 2019/12/9
 **/
@Component
@ConfigurationProperties("instance-description.properties")
public class TagsProperties {
  private Map<String, String> tag;

  public Map<String, String> getTag() {
    return tag;
  }

  public void setTag(Map<String, String> tag) {
    this.tag = tag;
  }
}
