package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @Author GuoYl123
 * @Date 2020/4/22
 **/
@RestController
public class ConsumerController {

  @Autowired
  private RestTemplate restTemplate;


  @RequestMapping("/hello")
  public String hello() {
    return restTemplate.getForObject("http://go-provider/hello", String.class);
  }
}
