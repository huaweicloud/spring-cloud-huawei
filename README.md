[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.huaweicloud/spring-cloud-huawei/badge.svg)](https://search.maven.org/search?q=g:com.huaweicloud%20AND%20a:spring-cloud-huawei-dependencies) 

# spring-cloud-huawei

[查看中文文档](./README_zh.md)

spring-cloud-huawei is a framework that makes it easier to integrate spring cloud and Huawei frameworks.
Including open source framework and commercial framework, 
open source such as 
[Apache ServiceComb](http://servicecomb.apache.org), 
commercial such as Huawei Cloud 
[ServiceStage](https://www.huaweicloud.com/product/servicestage.html).

### Why use
 1. Gives Blue-green Deployments and Canary Releases ability to your spring cloud application.
 2. Automatically generate a document, which follow open API specification, and register it to service registry, so that members of team can view and download API doc in unified service.
 3. Combined with the ecosystem of Apache servicecomb:
     - Apply multilingual paradigm to your microservice system by using [mesher](https://github.com/apache/servicecomb-mesher), an implementation of service mesh, java or spring cloud is not the only choice anymore.
     - Developer is able to use edge-service, a high-performance gateway which benifits from reactive, outperforms than spring cloud gateway and Netflix zuul.
     - A [go microservice framework](https://github.com/go-chassis/go-chassis) can work with springcloud.

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
     * Automatically generate swagger document with zero-code,
      and register to the server-center for interface documentation management.
     * Networking with [ServiceComb-Java-Chassis](https://github.com/apache/servicecomb-java-chassis) based swagger.
     * Using [Edge-Service](https://support.huaweicloud.com/bestpractice-servicestage/servicestage_bestpractice_0111.html) gateway，
 Better performance than spring cloud gateway and netflix zuul.

## How to use
spring-cloud-huawei has been released in Maven's central repository.

Dependent component version：
* Spring Cloud ：2.1.2.RELEASE
* Spring Boot ：2.1.6.RELEASE
* JDK ：1.8 +

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

## RoadMap
- [ ] Support WebFlux
