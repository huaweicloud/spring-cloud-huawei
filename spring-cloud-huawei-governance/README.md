# 基于动态配置的流量特征治理

### 背景:

 在实际使用场景中，存在不同的微服务框架混合的情况。例如使用Apache Dubbo和Spring Cloud的微服务希望和使用ServiceComb的微服务可以在统一的注册中心、配置中心下面进行协同工作。为此我们开发了[spring cloud huawei](https://github.com/huaweicloud/spring-cloud-huawei)、[dubbo-servicecomb](https://github.com/huaweicloud/dubbo-servicecomb)框架来使用spring cloud和dubbo开发的应用都可以使用servicecomb-servercenter、servicecomb-kie进行统一的管理。

 我们期望注册到同一个服务注册中心的任何框架的微服务(dubbo、spring cloud)都可以有相似的行为，对于服务治理我们也需要提供统一的标准来进行统一的下发以及统一的管理。
 除了相似的行为，服务治理本身如限流、降级、熔断、重试这些操作的生效应该集中在[运营运维阶段]，而不是[开发阶段]，开发人员可能难以在开发阶段就预知到某个接口需要进行限流或者熔断策略的配置。
 所以治理规则不应该和代码耦合，而应该从代码中独立出来。

 参考目前市面上的一些现有的著名治理框架，如sentinel、resilience4j等需要编写代码进行服务治理，其中resilience4j的治理是基于代码编写，无法做到实时的动态下发。
 sentinel提供了一套较为完整的解决方案，但需要专门的sentinel控制台才可以进行实时的治理规则修改，同时sentinel控制台提供了一些高级的集群流控等和管理面集成的方案。
 但缺点是依然需要在代码中定义sentinel资源来使业务和具体的治理框架耦合起来；并需要单独部署sentinel控制台，由此需要考虑高可用部署以及额外的部署资源。

 希望可以有一套**无代码侵入的**，**实时生效**，**低耦合易迁移**,**低成本**的治理方案来进行治理。
 为此我们定义了一套标准针对治理规则进行了标准化的定义。
 1. 通过流量特征来进行流量的标记和染色，对于http请求可以根据http header、http path、http method； 对于非http的任何rpc框架，也可以对于抽象出来的特征进行转换。比如进行context到header的转换。利用流量特征实现和任何具体rpc框架的解耦
 2. 通过动态配置来进行治理规则和代码的解耦，可以自由替换任何分布式配置中心。

 例如：

```yaml
servicecomb:
  matchGroup:
    demo-group: |
      matches:
        - apiPath:
            exact: "/hello"
          method:
            - GET
            - POST
          headers:
            user-num:
              exact: "xx"
          name: demo-name
  rateLimiting:
    limiterPolicy1: |
      match:
        rules: demo-group.demo-name
      timeoutDuration: 25
      limitRefreshPeriod: 1000
      limitForPeriod: 1
```

 规则解释：对于流量针对具体的http method、http uri、http header进行特征标记。对于具体的特征，可以配置相关的限流、熔断、重试的配置，来完成流量粒度的治理。


### 算子：

在match中提供了一系列的算子来对path或者header中的值进行匹配：

1. exact：
  精确匹配：模式字符串和目标字符串必须完全匹配。
2. regex:
  正则匹配：模式字符串为正则表达式来对目标字符串进行正则匹配，限制1s时间执行，防护正则注入攻击。
3. contains:
  包含： 目标字符串是否包含模式字符串。
4. compare:
  比较： 支持 >,<,>=,<=,=,!= 符号匹配，处理时会把模式字符串和目标字符串转化为double类型进行比较，支持的数据范围为double的数据范围。
  在进行 = 和 != 判断时 ， 如果二者的差值小于1e-6就视为相等。
  例如模式串为: >-10 会对大于-10以上的目标串匹配成功

### 模块：

#### 限流

简单配置：

```yaml
servicecomb:
  rateLimiting:
    xxxxx: |
      rules:
        match: demo-rateLimiting
      rate: 1
```
规则解释：
  作用在服务端，定义一个名为xxxxx的限流器，针对有demo-rateLimiting标记的流量，限制流量为rate(当前为1) req/s。

高级配置：

```yaml
servicecomb:
  rateLimiting:
    xxxxx: |
      rules:
        match: demo-rateLimiting
      timeoutDuration: 1000
      limitRefreshPeriod： 1000
      limitForPeriod： 1
```
规则解释：
 resilience4j 采用类似令牌桶的思想，其原理: 每隔limitRefreshPeriod的时间会加入limitForPeriod个新许可，
 如果获取不到新的许可(已经触发限流)，当前线程会park，最多等待timeoutDuration的时间，默认单位为ms。

#### 重试：

简单配置：

```yaml
servicecomb:
  retry:
    xxx: |
      rules:
        match: demo-retry
      maxAttempts: 3
      onSame: false
      retryOnResponseStatus： 502
      waitDuration：0
```
规则解释：
 作用在客户端，支持restemplate和feign两种客户端，针对有demo-retry标记的流量。
 对于返回http status 为 retryOnResponseStatus(默认值为502，如果有多个逗号分隔) 的请求，进行最大次数maxAttempts次的重试，
 每次重试间隔等待waitDuration(默认为0) ms。
 onSame为fasle会尽量选择不同实例进行重试(默认为false)，为true时不控制实例选择行为。

#### 熔断：

```yaml
servicecomb:
  circuitBreaker:
    xxx: |
      rules:
        match: demo-circuitBreaker
      failureRateThreshold：50
      slowCallRateThreshold： 50
      SlowCallDurationThreshold： 1000
      minimumNumberOfCalls: 2
      slidingWindowType: count
      slidingWindowSize: 2
```
规则解释：
 作用在服务端，达到指定failureRateThreshold错误率或者slowCallRateThreshold慢请求率时进行熔断，满请求通过SlowCallDurationThreshold定义,默认60s。
 minimumNumberOfCalls是达到熔断要求的最低请求数量门槛。
 slidingWindowType指定滑动窗口类型，默认可选count / time 分别是基于请求数量窗口和基于时间窗口。slidingWindowSize制定窗口大小，单位可能是请求数量或者秒。


#### 隔离

```yaml
servicecomb:
  bulkhead:
    xxx: |
      rules:
        match: demo-bulkhead
      maxConcurrentCalls: 1
      maxWaitDuration: 1000
```
规则解释：
 作用在服务端,针对制定的请求特征：最大并发线程量为maxConcurrentCalls，最大等待时间为maxWaitDuration。


#### 降级：

 实现 ClientRecoverPolicy 或者 ServerRecoverPolicy 并注入 bean 到 spring 容器中，来自定义如何处理异常情况。


