[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.huaweicloud/spring-cloud-huawei/badge.svg)](https://search.maven.org/search?q=g:com.huaweicloud%20AND%20a:spring-cloud-huawei-dependencies) 

# Spring Cloud Huawei [English document](README.md)

此框架的目的是为了让spring cloud 和华为的框架更好的融合在一起。
包括[Apache ServiceComb](http://servicecomb.apache.org) 和 [ServiceStage][ServiceStage] 。

## 支持的版本列表

| 分支 | 最新版本 | Spring Cloud基线版本 | Spring Boot基线版本 | 支持的Spring Cloud版本 | 支持的Spring Boot版本|
| ---------- | ------------ | ----------- | ----------- | ----------- | ----------- |
| master | 1.5.2-Hoxton | Hoxton.SR4 | 2.2.5.RELEASE | Hoxton | 2.2.x |
| Greenwich | 1.5.1-Greenwich | Greenwich.SR6 | 2.1.6-RELEASE | Greenwich | 2.1.x |
| Finchley | 1.5.1-Finchley | 2.0.4.RELEASE | 2.0.9.RELEASE     | Finchley     | 2.0.x       |
| Edgware | 1.2.0-Edgware | 1.3.6.RELEASE  | 1.5.22.RELEASE    | Edgware      | 1.5.x       |

***注意：***
* 查询 [Spring Cloud Release Train](https://spring.io/projects/spring-cloud) 选择一个适合业务的版本使用。
* Spring Cloud Edgeware, Finchley 已经停止维护，不建议在生产环境使用。 

## 为什么使用

1. Spring Cloud 使用 ServiceComb 提供的服务， 包括 servicecomb-service-center, servicecomb-kie等.
2. Spring Cloud 使用 ServiceStage 提供的服务. ServiceStage 是一个微服务的运行与托管平台，包括高可用的注册中心、配置中心、服务治理和分布式事务等服务。 
3. 给Spring Cloud应用提供契约生成和注册，灰度发布等功能。

## 功能模块

 * **spring-cloud-starter-huawei-servicecomb-discovery:**
     * 支持使用 [servicecomb-service-center](https://github.com/apache/servicecomb-service-center)
     * 支持使用 [CSE][CSE] 的 [服务中心][Service Registry]
     * 实现 DiscoveryClient, ReactiveDiscoveryClient
     * 实现 ServiceRegistry
     * 实现 ServerList, IPing, ServerListUpdater

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
 
 * **spring-cloud-starter-huawei-dtm:**
     * 支持使用 [ServiceStage][ServiceStage] [分布式事务][DTM]

 * **spring-cloud-starter-huawei-swagger:**
     * 自动契约生成和注册. 
     
## 如何使用

1. [开发指南](https://support.huaweicloud.com/devg-servicestage/ss-devg-0010.html)
2. [例子和快速入门](https://github.com/huaweicloud/spring-cloud-huawei-samples)

[ServiceStage]: https://support.huaweicloud.com/usermanual-servicestage/servicestage_user_0400.html
[CSE]: https://support.huaweicloud.com/devg-servicestage/ss-devg-0002.html
[DTM]: https://support.huaweicloud.com/devg-servicestage/dtm_devg_0002.html
[Service Registry]: https://support.huaweicloud.com/devg-servicestage/ss-devg-0017.html
[Configuration Center]: https://support.huaweicloud.com/devg-servicestage/ss-devg-0018.html
[Request Marker-based Governance]: https://support.huaweicloud.com/devg-servicestage/ss-devg-0020.html
[Canary release features]: https://support.huaweicloud.com/devg-servicestage/ss-devg-0023.html
