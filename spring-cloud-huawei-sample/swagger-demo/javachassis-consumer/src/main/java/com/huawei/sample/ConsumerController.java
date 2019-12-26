package com.huawei.sample;

import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

/**
 * @Author GuoYl123
 * @Date 2019/12/19
 **/
@RestSchema(schemaId = "javachassis-consumer")
@RequestMapping(path = "/consumer")
public class ConsumerController {

  private RestTemplate restTemplate = RestTemplateBuilder.create();

  @RequestMapping(path = "/helloFooRT", method = RequestMethod.GET)
  public Foo fooHelloRT(@RequestParam("id") int id) {
    Foo res = restTemplate
        .getForObject("cse://swagger-provider/foo?id=" + id, Foo.class);
    return res;
  }

  @RequestMapping(path = "/helloFoo", method = RequestMethod.GET)
  public Foo fooHello(@RequestParam("id") int id) {
    ProviderService helloService = Invoker.createProxy("swagger-provider", "provider-controller", ProviderService.class);
    return helloService.foo(id);
  }

  @RequestMapping(path = "/hello", method = RequestMethod.GET)
  public String hello() {
    String res = restTemplate
        .getForObject("cse://swagger-provider/hello", String.class);
    return res;
  }
}
