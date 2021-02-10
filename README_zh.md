[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.huaweicloud/spring-cloud-huawei/badge.svg)](https://search.maven.org/search?q=g:com.huaweicloud%20AND%20a:spring-cloud-huawei-dependencies) 

# Spring Cloud Huawei [English document](README.md)

此框架的目的是为了让spring cloud 和华为的框架更好的融合在一起。
包括[Apache ServiceComb](http://servicecomb.apache.org) 和 [ServiceStage](https://www.huaweicloud.com/product/servicestage.html) 。

## 支持的版本列表

| 分支 | 最新版本 | Spring Cloud基线版本 | Spring Boot基线版本 | 支持的Spring Cloud版本 | 支持的Spring Boot版本|
| ---------- | ------------ | ----------- | ----------- | ----------- | ----------- |
| master | 1.5.1-Hoxton | Hoxton.SR4 | 2.2.5.RELEASE | Hoxton | 2.2.x |
| Greenwich | 1.5.1-Greenwich | Greenwich.SR6 | 2.1.6-RELEASE | Greenwich | 2.1.x |
| Finchley | 1.5.1-Finchley | 2.0.4.RELEASE | 2.0.9.RELEASE     | Finchley     | 2.0.x       |
| Edgware | 1.2.0-Edgware | 1.3.6.RELEASE  | 1.5.22.RELEASE    | Edgware      | 1.5.x       |

***注意：***
* 查询 [Spring Cloud Release Train](https://spring.io/projects/spring-cloud) 选择一个适合业务的版本使用。
* Spring Cloud Edgeware, Finchley 已经停止维护，不建议在生产环境使用。 

## 为什么使用
 1. 提供路由管理功能，对应用进行蓝绿部署、灰度发布。
 2. 自动生成open-api规范的swagger契约文档，并注册到注册中心，以便统一查看、下载API文档。
 3. 与Apache ServiceComb的生态体系结合：
     - 使用reactive线程模型的高性能网关edge-service，表现优于spring cloud gateway和netflix zuul。
     - 利用[ServiceComb-Mesher](https://github.com/apache/servicecomb-mesher)实现多语言构建的微服务系统，mesher是service mesh的一种实现。
     - go语言微服务框架 [go-chassis](https://github.com/go-chassis/go-chassis)。
 4. 完全代码无侵入的核心思想，力求做到所有能力全部做在配置中，用户业务无感知，无迁移成本。


## 功能模块

 * **spring-cloud-starter-huawei-servicecomb-discovery:**
     * 对接华为云微服务引擎/[ServiceComb-Service-Center](https://github.com/apache/servicecomb-service-center)
 :一个基于Restful的提供微服务发现和微服务治理的服务注册中心，它基于Open API规范并提供服务发现、容错、动态路由、订阅和可扩展设计等功能。
 支持多环境、多维度管理，多注册中心配置。

 * **spring-cloud-starter-huawei-config:**
     * 对接华为云微服务引擎，进行配置管理，支持多环境、动态配置、全局配置、优先级多维度配置下发。
     * 对接[ServiceComb-Kie](https://github.com/apache/servicecomb-kie)，Kie是一个基于key-value的配置中心，支持历史版本、标签管理。

 * **spring-cloud-starter-huawei-dtm:**
     * 对接华为云分布式事务引擎DTM，解决分布式环境下事务一致性问题。

 * **spring-cloud-starter-huawei-router:**
     * 路由管理模块，通过配置实现灰度发布、金丝雀发布、流量分配管理，支持匹配http header、比例分配流量。

 * **spring-cloud-starter-huawei-swagger:**
     * 基于代码零配置自动生成swagger接口契约(基于[spring-fox](https://github.com/springfox/springfox)的能力)，自动注册到Service-Center注册中心进行接口文档化管理。
     * 基于契约与微服务框架[ServiceComb-Java-Chassis](https://github.com/apache/servicecomb-java-chassis)组网。
     * 使用[Edge-Service](https://docs.servicecomb.io/java-chassis/zh_CN/edge/by-servicecomb-sdk/)网关，
 [表现](https://github.com/AngLi2/api-gateway-benchmark/blob/master/Spring%20Cloud%20Gateway%2C%20Zuul%2C%20Edge%20Service%20%E6%80%A7%E8%83%BD%E5%AF%B9%E6%AF%94.md)
 优于spring cloud gateway和netflix zuul，体验reactive带来的性能提升。

 * **spring-cloud-starter-huawei-governance:**
     * 服务治理模块，提供基于动态配置的熔断、限流、隔离、重试功能特性，核心能力基于[resilience4j](https://github.com/resilience4j/resilience4j)。
     * 流量粒度的治理管控，可以针对请求Path、请求Method、请求Header进行相应的算子匹配来进行流量标记，进行精确的流量治理。
     * 利用动态配置，做到零等待下发治理规则生效。无代码侵入，用户无需学习繁琐的sdk使用，只需下发配置。
     * [更多信息](https://github.com/huaweicloud/spring-cloud-huawei/tree/master/spring-cloud-huawei-governance)
     
## 如何使用
spring-cloud-huawei已发布在maven中央仓库。

使用dependencyManagement引入依赖。

    <dependencyManagement>
      <dependencies>
        <dependency>
          <groupId>com.huaweicloud</groupId>
          <artifactId>spring-cloud-huawei-dependencies</artifactId>
          <version>${project.version}</version>
          <type>pom</type>
          <scope>import</scope>
        </dependency>
      </dependencies>
    </dependencyManagement>
    
引入相应starter。

    <dependency>
        <groupId>com.huaweicloud</groupId>
        <artifactId>spring-cloud-starter-huawei-servicecomb-discovery</artifactId>
    </dependency>
    
[更多文档](https://support.huaweicloud.com/devg-servicestage/cse_java_0054.html)
