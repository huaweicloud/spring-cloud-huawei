[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.huaweicloud/spring-cloud-huawei/badge.svg)](https://search.maven.org/search?q=g:com.huaweicloud%20AND%20a:spring-cloud-huawei-dependencies) 

# Spring Cloud Huawei [查看中文文档](README_zh.md)

Spring Cloud Huawei is a framework that makes it easier to integrate Spring Cloud and Huawei frameworks.
Including [Apache ServiceComb](http://servicecomb.apache.org) and [ServiceStage](https://www.huaweicloud.com/product/servicestage.html).

## Supported version

| Branch | Latest Version | Spring Cloud Base Version | Spring Boot Base Version | Supported Spring Cloud Version | Supported Spring Boot Version|
| ---------- | ------------ | ----------- | ----------- | ----------- | ----------- |
| master | 1.5.1-Hoxton | Hoxton.SR4 | 2.2.5.RELEASE | Hoxton | 2.2.x |
| Greenwich | 1.5.1-Greenwich | Greenwich.SR6 | 2.1.6-RELEASE | Greenwich | 2.1.x |
| Finchley | 1.5.1-Finchley | 2.0.4.RELEASE | 2.0.9.RELEASE     | Finchley     | 2.0.x       |
| Edgware | 1.2.0-Edgware | 1.3.6.RELEASE  | 1.5.22.RELEASE    | Edgware      | 1.5.x       |

***Notice：***
* Check [Spring Cloud Release Train](https://spring.io/projects/spring-cloud), and find a proper version to use.
* Spring Cloud Edgeware, Finchley have all reached end of life status and are no longer supported.

## Why use
 1. Gives Blue-green Deployments and Canary Releases ability to your spring cloud application.
 2. Automatically generate a document, which follow open API specification, and register it to service registry, so that members of team can view and download API doc in unified service.
 3. Combined with the ecosystem of Apache servicecomb:
     - Apply multilingual paradigm to your microservice system by using [mesher](https://github.com/apache/servicecomb-mesher), an implementation of service mesh, java or spring cloud is not the only choice anymore.
     - Developer is able to use edge-service, a high-performance gateway which benifits from reactive, outperforms than spring cloud gateway and Netflix zuul.
     - A [go microservice framework](https://github.com/go-chassis/go-chassis) can work with springcloud.
 4. Zero-code thinking, all features are based on configuration, transparent to users, no migration costs.

## Modules

 * **spring-cloud-starter-huawei-servicecomb-discovery:**
     * Support for use HuaweiCloud CSE/[ServiceComb-Service-Center](https://github.com/apache/servicecomb-service-center)
   :is a Restful based service-registry that provides 
   micro-services discovery and micro-service management. It is based on Open API format 
   and provides features like service-discovery, fault-tolerance, dynamic routing, 
   notify subscription and scalable by design.
   Support multi-environment, multi-dimensional management and multi-registry configuration.

 * **spring-cloud-starter-huawei-config:**
     * Connect with HuaweiCloud CSE for configuration management. 
     Support multi environment, dynamic configuration, global configuration and
      priority multi-dimensional configuration distribution.
     * Support [ServiceComb-Kie](https://github.com/apache/servicecomb-kie),
      KIE is a key value based configuration center that supports historical version and label management.
 
 * **spring-cloud-starter-huawei-dtm:**
     * Support for use HuaweiCloud DTM(Distributed Transaction Management),it resolve consistency issues in a distributed environment.

 * **spring-cloud-starter-huawei-router:**
     * The routing management module , for gray release,canary release by config, 
 it supports matching HTTP header and proportional traffic distribution.
 
 * **spring-cloud-starter-huawei-swagger:**
     * Automatically generate swagger document with zero-code (based on [spring-fox](https://github.com/springfox/springfox)),
      and register to the server-center for interface documentation management.
     * Networking with [ServiceComb-Java-Chassis](https://github.com/apache/servicecomb-java-chassis) based swagger.
     * Using [Edge-Service](https://docs.servicecomb.io/java-chassis/en_US/edge/by-servicecomb-sdk/) gateway，
 Better performance than spring cloud gateway and netflix zuul.

 * **spring-cloud-starter-huawei-governance:**
     * Based on the dynamic configuration, provide CircuitBreaker, Bulkhead, RateLimiter and Retry feature, based on [resilience4j](https://github.com/resilience4j/resilience4j)。
     * For traffic governance, the operator matching is used to label the traffic, which can accurately govern the traffic for the request path , request method and request header.
     * By using dynamic configuration, zero waiting for governance rules to take effect. No code intrusion, only need distribution configuration.
     * [more information](https://github.com/huaweicloud/spring-cloud-huawei/tree/master/spring-cloud-huawei-governance)

## How to use
spring-cloud-huawei has been released in Maven's central repository.

Use dependencyManagement to manage dependencies.

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
    
        
introduce starter.

    <dependency>
        <groupId>com.huaweicloud</groupId>
        <artifactId>spring-cloud-starter-huawei-servicecomb-discovery</artifactId>
    </dependency>
    
[more document](https://support.huaweicloud.com/devg-servicestage/cse_java_0054.html)
