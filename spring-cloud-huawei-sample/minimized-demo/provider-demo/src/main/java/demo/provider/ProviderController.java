package demo.provider;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author GuoYl123
 * @Date 2020/4/22
 **/
@RestController
public class ProviderController {

  @RequestMapping("/hello")
  public String sayHello() {
    return "Hello world!";
  }
}
