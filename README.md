[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.huaweicloud/spring-cloud-huawei/badge.svg)](https://search.maven.org/search?q=g:com.huaweicloud%20AND%20a:spring-cloud-huawei-dependencies) 

# Spring Cloud Huawei [查看中文文档](README_CN.md)

Spring Cloud Huawei is a framework that makes it easier and productive to develop microservices with Spring Cloud. Spring Cloud Huawei using 
[Apache ServiceComb][SERVICECOMB] and [CSE][CSE] registry (servicecomb-service-center) and config (servicecomb-kie).

## Supported version

| Branch    | Spring Cloud Huawei Latest Version | Compiled Spring Cloud Version | Compiled Spring Boot Version |
|-----------|------------------------------------|-------------------------------|------------------------------|
| master    | 1.10.8-2021.0.x                    | 2021.0.5                      | 2.6.13                       | 
| 2020.0.x  | 1.10.8-2020.0.x                    | 2020.0.6                      | 2.5.14                       |
| Hoxton    | 1.9.3-Hoxton                       | Hoxton.SR9                    | 2.3.5.RELEASE                |
| Greenwich | 1.6.1-Greenwich                    | Greenwich.SR6                 | 2.1.6-RELEASE                |
| Finchley  | 1.6.1-Finchley                     | 2.0.4.RELEASE                 | 2.0.9.RELEASE                |
| Edgware   | 1.2.0-Edgware                      | 1.3.6.RELEASE                 | 1.5.22.RELEASE               |

***Notice：***
* You can use `Spring Cloud` compatible version to use `Spring Cloud Huawei`. See https://spring.io/projects/spring-cloud for more details.
* Spring Cloud Edgeware, Finchley, Greenwich, Hoxton have all reached end of life status and
  suggest not for production use. Check [Spring Cloud Releases][Spring Cloud Releases] for details.
* Before Hoxton(include), Netfix OSS like Ribbon, Hystrix are supported. After 2020.0.x(include),
  Spring Cloud Loadbalancer are supported.
* Before 2020.0.x(include)， springfox is used for swagger generation. After 2021.0.x(include),
  springdoc is used for swagger generation. 

## How to use

1. [Quick Start and Developer Guide](https://github.com/huaweicloud/spring-cloud-huawei/wiki)
2. [Samples](https://github.com/huaweicloud/spring-cloud-huawei-samples)
3. [CSE Guide][CSE Developer Guide]
4. [ServiceStage Guide][ServiceStage]

## Modules

 * **spring-cloud-starter-huawei-discovery:**
     * Support for use [servicecomb-service-center](https://github.com/apache/servicecomb-service-center)
     * Support for use [CSE][CSE] as [Service Registry][Service Registry]
     * Implements DiscoveryClient, ReactiveDiscoveryClient
     * Implements ServiceRegistry
     
 * **spring-cloud-starter-huawei-config:**
     * Support for use [servicecomb-kie](https://github.com/apache/servicecomb-kie)
     * Support for use [CSE][CSE] as [Configuration Center][Configuration Center]
     * Implements PropertySource
     * Implements @RefreshScope

 * **spring-cloud-starter-huawei-governance:**
     * Support for use [Request Marker-based Governance][Request Marker-based Governance]
     * Implements retry for FeignClient and RestTemplate
     * Implements rate limiter, circuit breaker, and bulkhead for Servlet Web MVC

 * **spring-cloud-starter-huawei-router:**
     * Support for use [ServiceStage][ServiceStage] [Canary release features][Canary release features].

 * **spring-cloud-starter-huawei-swagger:**
     * Automatically swagger document generation and registration. 

* **spring-cloud-starter-huawei-jasypt:**
     * Support for use [Profile encryption scheme][Profile encryption scheme].
     * Implements the function of identifying encryption mark in service configuration file


[ServiceStage]: https://support.huaweicloud.com/intl/en-us/productdesc-servicestage/ss_productdesc_0001.html
[CSE]: https://www.huaweicloud.com/intl/en-us/product/cse.html
[CSE Developer Guide]: https://support.huaweicloud.com/intl/en-us/devg-cse/cse_devg_0002.html
[SERVICECOMB]: https://servicecomb.apache.org/developers/
[Service Registry]: https://support.huaweicloud.com/intl/en-us/devg-servicestage/ss-devg-0017.html
[Configuration Center]: https://support.huaweicloud.com/intl/en-us/devg-servicestage/ss-devg-0018.html
[Request Marker-based Governance]: https://support.huaweicloud.com/intl/en-us/devg-servicestage/ss-devg-0020.html
[Canary release features]: https://support.huaweicloud.com/devg-servicestage/ss-devg-0023.html
[Profile encryption scheme]: https://support.huaweicloud.com/bestpractice-cse/cse_bestpractice_0007.html
[Spring Cloud Releases]: https://github.com/spring-cloud/spring-cloud-release/wiki/Supported-Versions
