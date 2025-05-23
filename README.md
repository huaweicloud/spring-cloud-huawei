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

| Branch    | Spring Cloud Huawei Latest Version | Compiled Spring Cloud Version | Compiled Spring Boot Version | Tested JDK Version | Notes          |
|-----------|------------------------------------|-------------------------------|------------------------------|--------------------|----------------|
| master    | 1.11.11-2024.0.x(推荐)               | 2024.0.1                      | 3.4.4                        | OpenJDK 17         |                |
| 2023.0.x  | 1.11.11-2023.0.x(推荐)               | 2023.0.5                      | 3.3.10                       | OpenJDK 17         |                |
| 2022.0.x  | 1.11.10-2022.0.x                   | 2022.0.5                      | 3.1.12                       | OpenJDK 17         | End of Support |
| 2021.0.x  | 1.11.11-2021.0.x(推荐)               | 2021.0.9                      | 2.7.18                       | OpenJDK 8          | Vulnerability  |
| 2020.0.x  | 1.10.8-2020.0.x                    | 2020.0.6                      | 2.5.14                       | OpenJDK 8          | End of Support |
| Hoxton    | 1.9.4-Hoxton                       | Hoxton.SR9                    | 2.3.5.RELEASE                | OpenJDK 8          | End of Support |
| Greenwich | 1.6.4-Greenwich                    | Greenwich.SR6                 | 2.1.6-RELEASE                | OpenJDK 8          | End of Support |
| Finchley  | 1.6.1-Finchley                     | 2.0.4.RELEASE                 | 2.0.9.RELEASE                | OpenJDK 8          | End of Support |
| Edgware   | 1.2.0-Edgware                      | 1.3.6.RELEASE                 | 1.5.22.RELEASE               | OpenJDK 8          | End of Support |

***Notice：***
* Upgrade to the OOS version 2023.0.x/2024.0.X of the Spring community as soon as possible, because of 2021.0.x branch dependence Spring 5.3.x has certain vulnerabilities.
* You can use `Spring Cloud` compatible version to use `Spring Cloud Huawei`. See https://spring.io/projects/spring-cloud for more details.
* Spring Cloud Edgeware, Finchley, Greenwich, Hoxton, 2020.0.x have all reached end of life status and
  suggest not for production use. Check [Spring Cloud Releases][Spring Cloud Releases] for details.
* Before Hoxton(include), Netfix OSS like Ribbon, Hystrix are supported. After 2020.0.x(**include**),
  Spring Cloud Loadbalancer are supported.
* Before 2020.0.x(include)， springfox is used for swagger generation. After 2021.0.x(include),
  springdoc is used for swagger generation. 
* After 2022.0.x(include), JDK 17+ is needed to run.
* Spring Cloud Huawei historical versions Spring Boot, Spring Cloud dependency description reference [version description][Spring Cloud Huawei Releases].

## How to use

1. [Quick Start and Developer Guide](https://github.com/huaweicloud/spring-cloud-huawei/wiki)
2. [Samples](https://github.com/huaweicloud/spring-cloud-huawei-samples)
3. [CSE Guide][CSE Developer Guide]
4. [ServiceStage Guide][ServiceStage]

[ServiceStage]: https://support.huaweicloud.com/intl/en-us/usermanual-servicestage/servicestage_03_0001.html
[CSE]: https://www.huaweicloud.com/intl/en-us/product/cse.html
[CSE Developer Guide]: https://support.huaweicloud.com/intl/en-us/productdesc-cse/cse_productdesc_0001.html
[SERVICECOMB]: https://servicecomb.apache.org/developers/
[NACOS]: https://nacos.io/zh-cn/index.html
[Service Registry]: https://support.huaweicloud.com/intl/en-us/devg-servicestage/ss-devg-0017.html
[Configuration Center]: https://support.huaweicloud.com/intl/en-us/devg-servicestage/ss-devg-0018.html
[Request Marker-based Governance]: https://support.huaweicloud.com/intl/en-us/devg-servicestage/ss-devg-0020.html
[Canary release features]: https://support.huaweicloud.com/devg-servicestage/ss-devg-0023.html
[Profile encryption scheme]: https://support.huaweicloud.com/bestpractice-cse/cse_bestpractice_0007.html
[Spring Cloud Releases]: https://github.com/spring-cloud/spring-cloud-release/wiki/Supported-Versions
[Spring Cloud Huawei Releases]: https://github.com/huaweicloud/spring-cloud-huawei/wiki/third-version-support-descriptions

## Star this project

If you like this project, do not forget star it.

[![Star History Chart](https://api.star-history.com/svg?repos=huaweicloud/spring-cloud-huawei&type=Date)](https://star-history.com/#huaweicloud/spring-cloud-huawei&Date)
