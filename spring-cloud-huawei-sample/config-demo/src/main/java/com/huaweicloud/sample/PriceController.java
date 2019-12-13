package com.huaweicloud.sample;

import com.huaweicloud.config.ConfigRefreshEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
public class PriceController implements ApplicationListener<ConfigRefreshEvent> {

  @Value("${server.port}")
  private Integer port;

  @Value("${dd:}")
  private String dd;

  /**
   * 服务接口
   * @param id
   * @return
   */
  @RequestMapping("/price")
  public String sayHello(@RequestParam("id") String id) {

    return "price ---> " + id + " port -->" + dd;
  }

  public void onApplicationEvent(ConfigRefreshEvent event) {
    System.out.println("change = [" + event.getChange() + "]");
  }
}
