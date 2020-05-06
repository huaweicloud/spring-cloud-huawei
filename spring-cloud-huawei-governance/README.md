# 流量特征治理

增加流量特征治理特性

#### 背景:

 在实际使用场景中，存在不同的微服务框架混合的情况。例如使用Apache Dubbo和Spring Cloud的微服务希望和使用ServiceComb的微服务可以在统一的注册中心、配置中心下面进行协同工作。为此我们开发了[spring cloud huawei](https://github.com/huaweicloud/spring-cloud-huawei)、[dubbo-servicecomb](https://github.com/huaweicloud/dubbo-servicecomb)框架来使用spring cloud和dubbo开发的应用都可以使用servicecomb-servercenter、servicecomb-kie进行统一的管理。

  我们期望注册到同一个servicecomb-servercenter、servicecomb-kie的任何框架的微服务(dubbo、spring cloud)都可以有相似的行为，对于服务治理我们也需要提供统一的标准来进行统一的下发以及统一的管理。

为此我们定义了一套标准针对治理规则进行了标准化的定义。比如：

```yaml
servicecomb:
  match:
    traffic-to-some-api-from-jack: |
      matches:
        - apiPath:
            exact: "/hello"
          method:
            - GET
            - POST
          trafficMarkPolicy: once
  rateLimiting:
    limiterPolicy1: |
      match: traffic-to-some-api-from-jack
      timeoutDuration: 25
      limitRefreshPeriod: 1000
      limitForPeriod: 1
```

 规则解释：对于流量针对具体的http method、http uri、http header进行特征标记。对于具体的特征，可以配置相关的限流、熔断、重试的配置，来完成流量粒度的治理。

#### 实现：

这套规则目前在spring cloud huawei项目中已经有了[实现](https://github.com/GuoYL123/spring-cloud-huawei/tree/gover/spring-cloud-huawei-governance)。后续考虑在java chassis中加入对该功能的支持。

在实现过程中，定义一套interface和spec，叫做：servicecomb runtime interface ( SCRI )，基于

Netfilx archaius的基础上实现一套可以获取标准spec的治理Policy的interface方便用户进行拓展。

#### 价值：

1. 对于spring cloud 、dubbo、servicecomb的微服务为用户提供统一的体验。
2. 拓展servicecomb server center、servicecomb kie在其他流行框架中如spring cloud、dubbo的能力和影响力。

