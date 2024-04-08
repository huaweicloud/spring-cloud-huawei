[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.huaweicloud/spring-cloud-huawei/badge.svg)](https://search.maven.org/search?q=g:com.huaweicloud%20AND%20a:spring-cloud-huawei-dependencies)

# Spring Cloud Huawei [English document](README.md)

Spring Cloud Huawei的目的是为了让开发者更加方便、高效的使用Spring Cloud开发可靠的微服务应用。 

Spring Cloud Huawei 支持 [Apache ServiceComb][SERVICECOMB] and [Nacos][NACOS] 作为服务注册发现中心和服务配置管理中心。 

Spring Cloud Huawei提供了大量开箱即用的服务治理能力，让开发者能够快速上手构建韧性、可靠的微服务应用。

| 类别    | CSE(ServiceComb)           | CSE(Nacos)     |
|-------|----------------------------|----------------|
| 注册中心  | servicecomb-service-center | nacos          |
| 配置中心  | servicecomb-kie            | nacos          |

[CSE][CSE] 提供了 ServiceComb 和 Nacos 的商业版本。

## 支持的版本列表

| 分支        | 最新版本            | 编译使用 Spring Cloud版本 | 编译使用 Spring Boot版本 |
|-----------|-----------------|---------------------|--------------------|
| master    | 1.11.4-2021.0.x | 2021.0.8            | 2.7.17             | 
| 2020.0.x  | 1.10.8-2020.0.x | 2020.0.6            | 2.5.14             |
| Hoxton    | 1.9.3-Hoxton    | Hoxton.SR9          | 2.3.5.RELEASE      |
| Greenwich | 1.6.3-Greenwich | Greenwich.SR6       | 2.1.6-RELEASE      |

***注意：***
* 使用`Spring Cloud Huawei`时可以使用兼容的`Spring Cloud`版本。详细信息可参考 https://spring.io/projects/spring-cloud 。
* Spring Cloud Edgeware, Finchley, Greenwich, Hoxton 已经停止维护，不建议在生产环境使用。
  详细情况可以查询 [Spring Cloud Releases][Spring Cloud Releases]。
* Hoxton(包括)之前的版本支持Netfix OSS的组件，比如Ribbon, Hystrix. 2020.0.x(包括)之后的版本支持
  Spring Cloud Loadbalancer.
* 2020.0.x(包括)之前的版本使用springfox生成swagger. 2021.0.x(包括)之后的版本使用springdoc生成swagger.

## 如何使用

1. [快速入门和开发指南](https://github.com/huaweicloud/spring-cloud-huawei/wiki)
2. [例子](https://github.com/huaweicloud/spring-cloud-huawei-samples)
3. [CSE帮助文档][CSE Developer Guide]
4. [ServiceStage帮助文档][ServiceStage]

[ServiceStage]: https://support.huaweicloud.com/usermanual-servicestage/servicestage_user_0400.html

[CSE]: https://support.huaweicloud.com/cse/index.html

[CSE Developer Guide]: https://support.huaweicloud.com/devg-cse/cse_devg_0002.html

[SERVICECOMB]: https://servicecomb.apache.org/cn/developers/

[NACOS]: https://nacos.io/zh-cn/index.html

[Service Registry]: https://support.huaweicloud.com/devg-cse/cse_devg_0018.html

[Configuration Center]: https://support.huaweicloud.com/devg-cse/cse_devg_0020.html

[Request Marker-based Governance]: https://github.com/huaweicloud/spring-cloud-huawei/wiki/using-governance

[Canary release features]: https://support.huaweicloud.com/devg-servicestage/ss-devg-0023.html

[Profile encryption scheme]: https://support.huaweicloud.com/bestpractice-cse/cse_bestpractice_0007.html

[Spring Cloud Releases]: https://github.com/spring-cloud/spring-cloud-release/wiki/Supported-Versions
