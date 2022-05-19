package com.huaweicloud.sample;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class GatewayIT {
  final String url = "http://127.0.0.1:10088";

  final RestTemplate template = new RestTemplate();

  @Test
  public void testGetOrder() {
    String result = template.getForObject(url + "/order/order?id=hello", String.class);
    assertThat(result).isEqualTo("hello");
  }
}
