[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.huaweicloud/spring-cloud-huawei/badge.svg)](https://search.maven.org/search?q=g:com.huaweicloud%20AND%20a:spring-cloud-huawei-dependencies)

# Spring Cloud Huawei [English document](README.md)

Spring Cloud Huawei的目的是为了让Spring Cloud使用
[Apache ServiceComb](http://servicecomb.apache.org)和 [CSE][CSE] 。

## 支持的版本列表

| 分支        | 最新版本            | 编译使用 Spring Cloud版本 | 编译使用 Spring Boot版本 |
|-----------|-----------------|---------------------|--------------------|
| master    | 1.10.2-2021.0.x | 2021.0.3            | 2.6.7              | 
| 2020.0.x  | 1.10.2-2020.0.x | 2020.0.5            | 2.5.12             |
| Hoxton    | 1.9.3-Hoxton    | Hoxton.SR9          | 2.3.5.RELEASE      |
| Greenwich | 1.6.1-Greenwich | Greenwich.SR6       | 2.1.6-RELEASE      |
| Finchley  | 1.6.1-Finchley  | 2.0.4.RELEASE       | 2.0.9.RELEASE      |
| Edgware   | 1.2.0-Edgware   | 1.3.6.RELEASE       | 1.5.22.RELEASE     |

***注意：***
* 使用`Spring Cloud Huawei`时可以使用兼容的`Spring Cloud`版本。详细信息可参考 https://spring.io/projects/spring-cloud。
* Spring Cloud Edgeware, Finchley, Greenwich, Hoxton 已经停止维护，不建议在生产环境使用。
  详细情况可以查询 [Spring Cloud Releases][Spring Cloud Releases]。
* Hoxton(包括)之前的版本支持Netfix OSS的组件，比如Ribbon, Hystrix. 2020.0.x(包括)之后的版本支持
  Spring Cloud Loadbalancer.
* 2020.0.x(包括)之前的版本使用springfox生成swagger. 2021.0.x(包括)之后的版本使用
    springdoc生成swagger.

## 为什么使用

1. Spring Cloud 使用 CSE 提供的服务. CSE 是一个微服务的运行与托管平台，包括高可用的注册中心、配置中心、服务治理等服务。
2. Spring Cloud 使用开源项目 [Apache ServiceComb][ServiceComb] 提供的服务， 包括 servicecomb-service-center, servicecomb-kie等 。
3. 给Spring Cloud应用提供契约生成和注册，灰度发布等功能。

## 功能模块

* **spring-cloud-starter-huawei-discovery:**
    * 支持使用 [servicecomb-service-center](https://github.com/apache/servicecomb-service-center)
    * 支持使用 [CSE][CSE] 的 [服务中心][Service Registry]
    * 实现 DiscoveryClient, ReactiveDiscoveryClient
    * 实现 ServiceRegistry

* **spring-cloud-starter-huawei-config:**
    * 支持使用 [servicecomb-kie](https://github.com/apache/servicecomb-kie)
    * 支持使用 [CSE][CSE] 的 [配置中心][Configuration Center]
    * 实现 PropertySource
    * 实现 @RefreshScope

* **spring-cloud-starter-huawei-governance:**
    * 支持使用  [CSE][CSE] [基于动态配置的流量特征治理][Request Marker-based Governance]
    * 实现 FeignClient and RestTemplate 的重试
    * 实现 Servlet Web MVC 的限流、熔断器和隔离仓

* **spring-cloud-starter-huawei-router:**
    * 支持使用  [ServiceStage][ServiceStage] [灰度发布][Canary release features].

* **spring-cloud-starter-huawei-swagger:**
    * 自动契约生成和注册.

* **spring-cloud-starter-huawei-jasypt:**
    * 支持使用[配置文件加密方案][Profile encryption scheme]
    * 实现服务配置文件识别加密标记功能.

## 如何使用

1. [快速入门和开发指南](https://github.com/huaweicloud/spring-cloud-huawei/wiki)
2. [例子](https://github.com/huaweicloud/spring-cloud-huawei-samples)
3. [CSE帮助文档][CSE]

[ServiceStage]: https://support.huaweicloud.com/usermanual-servicestage/servicestage_user_0400.html

[CSE]: https://support.huaweicloud.com/cse/index.html

[Service Registry]: https://support.huaweicloud.com/devg-cse/cse_devg_0018.html

[Configuration Center]: https://support.huaweicloud.com/devg-cse/cse_devg_0020.html

[Request Marker-based Governance]: https://support.huaweicloud.com/devg-cse/cse_devg_0025.html

[Canary release features]: https://support.huaweicloud.com/devg-servicestage/ss-devg-0023.html

[ServiceComb]: http://servicecomb.apache.org/cn/developers/

[Profile encryption scheme]: https://support.huaweicloud.com/bestpractice-cse/cse_bestpractice_0007.html

[Spring Cloud Releases]: https://github.com/spring-cloud/spring-cloud-release/wiki/Supported-Versions