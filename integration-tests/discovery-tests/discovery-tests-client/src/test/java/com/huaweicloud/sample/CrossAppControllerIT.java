package com.huaweicloud.sample;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

public class CrossAppControllerIT {
  String url = "http://127.0.0.1:9098";

  int crossAppPricePort = 9092;

  RestTemplate template = new RestTemplate();

  @Test
  public void testGetOrder() {
    String result = template.getForObject(url + "/crossapporder?id=hello", String.class);
    assertThat(result).isEqualTo("hello");
  }

  @Test
  @SuppressWarnings({"rawTypes", "unckecked"})
  public void testGetInstances() {
    List result = template.getForObject(url + "/crossappinstances", List.class);
    assertThat(result.size()).isEqualTo(1);
    Map instance = (Map) result.get(0);
    assertThat(instance.get("port")).isEqualTo(crossAppPricePort);
  }
}
