[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.huaweicloud/spring-cloud-huawei/badge.svg)](https://search.maven.org/search?q=g:com.huaweicloud%20AND%20a:spring-cloud-huawei-dependencies) 

# Spring Cloud Huawei [查看中文文档](README_CN.md)

Spring Cloud Huawei is a framework that makes it easier and productive to develop microservices with Spring Cloud. 

Spring Cloud Huawei supports 
[Apache ServiceComb][SERVICECOMB] and [Nacos][NACOS] as discovery, registration and configuration management service. 

Spring Cloud Huawei provides a large number of out-of-the-box service governance capabilities, enabling developers to quickly build resilient and reliable microservice applications.

| Service | CSE(ServiceComb)           | CSE(Nacos)     |
|--|----------------------------|----------------|
| Discovery and Registration | servicecomb-service-center | nacos          |
| Configuration Management | servicecomb-kie            | nacos          |

[CSE][CSE] provides commercial versions of ServiceComb and Nacos.

## Supported version

| Branch    | Spring Cloud Huawei Latest Version | Compiled Spring Cloud Version | Compiled Spring Boot Version |
|-----------|------------------------------------|-------------------------------|------------------------------|
| master    | 1.11.4-2022.0.x                    | 2022.0.4                      | 3.1.5                        | 
| 2021.0.x  | 1.11.4-2021.0.x                    | 2021.0.8                      | 2.7.17                       | 
| 2020.0.x  | 1.10.8-2020.0.x                    | 2020.0.6                      | 2.5.14                       |
| Hoxton    | 1.9.3-Hoxton                       | Hoxton.SR9                    | 2.3.5.RELEASE                |
| Greenwich | 1.6.3-Greenwich                    | Greenwich.SR6                 | 2.1.6-RELEASE                |

***Notice：***
* You can use `Spring Cloud` compatible version to use `Spring Cloud Huawei`. See https://spring.io/projects/spring-cloud for more details.
* Spring Cloud Edgeware, Finchley, Greenwich, Hoxton, 2020.0.x have all reached end of life status and
  suggest not for production use. Check [Spring Cloud Releases][Spring Cloud Releases] for details.
* Before Hoxton(include), Netfix OSS like Ribbon, Hystrix are supported. After 2020.0.x(**include**),
  Spring Cloud Loadbalancer are supported.
* Before 2020.0.x(include)， springfox is used for swagger generation. After 2021.0.x(include),
  springdoc is used for swagger generation. 
* After 2022.0.x(include), JDK 17+ is needed to run.

## How to use

1. [Quick Start and Developer Guide](https://github.com/huaweicloud/spring-cloud-huawei/wiki)
2. [Samples](https://github.com/huaweicloud/spring-cloud-huawei-samples)
3. [CSE Guide][CSE Developer Guide]
4. [ServiceStage Guide][ServiceStage]

[ServiceStage]: https://support.huaweicloud.com/intl/en-us/usermanual-servicestage/servicestage_03_0001.html
[CSE]: https://www.huaweicloud.com/intl/en-us/product/cse.html
[CSE Developer Guide]: https://support.huaweicloud.com/intl/en-us/devg-cse/cse_devg_0002.html
[SERVICECOMB]: https://servicecomb.apache.org/developers/
[NACOS]: https://nacos.io/zh-cn/index.html
[Service Registry]: https://support.huaweicloud.com/intl/en-us/devg-servicestage/ss-devg-0017.html
[Configuration Center]: https://support.huaweicloud.com/intl/en-us/devg-servicestage/ss-devg-0018.html
[Request Marker-based Governance]: https://support.huaweicloud.com/intl/en-us/devg-servicestage/ss-devg-0020.html
[Canary release features]: https://support.huaweicloud.com/devg-servicestage/ss-devg-0023.html
[Profile encryption scheme]: https://support.huaweicloud.com/bestpractice-cse/cse_bestpractice_0007.html
[Spring Cloud Releases]: https://github.com/spring-cloud/spring-cloud-release/wiki/Supported-Versions
