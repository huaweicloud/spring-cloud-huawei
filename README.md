[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
# spring-cloud-huawei

[查看中文文档](./README_zh.md)

spring-cloud-huawei is a framework that makes it easier to integrate spring cloud and Huawei frameworks.
Including open source framework and commercial framework, 
open source such as 
[Apache ServiceComb](http://servicecomb.apache.org), 
commercial such as Huawei Cloud 
[ServiceStage](https://www.huaweicloud.com/product/servicestage.html).
## Features
### open source
- [x] **Spring Cloud use ServiceComb-Service-Center to implement registration and discovery:**
No need to change the code, just modify the individual configuration files(application.yml) to complete the migration.
Multiple Service-Centers can be launched and the client will select one of the healthy Service-Centers to make the call.

### Huawei Cloud
- [x] **Spring Cloud use ServiceStage to implement registration and discovery:**
No need to change the code, just modify the individual configuration files(application.yml) to complete the migration.

- [x] **Spring Cloud use ServiceStage to implement Distributed Transaction:**
(Distributed Transaction Management, DTM) is a product that addresses consistency issues in a distributed environment.

- [x] **Spring Cloud uses ServiceStage distributed configuration service:**
Support for getting configuration from the CSE microservice engine /  ServiceComb kie, and dynamic updates, following the way of Spring Cloud.

- [x] **Spring Cloud uses ServiceStage gray release service:**
Support for gray release capabilities.

- [x] **Spring Cloud registry swagger to server center :**
Support automatic generated swagger documentation without configuration, and registry swagger to ServiceComb server center.

- [x] **Spring Cloud registry networking with ServiceComb Java-Chassis / Edge-Service :**
Support for use reactive framework ServiceComb Java-Chassis and gateway ServiceComb Edge-Service which have the better performance. 

## Components

 * [Apache-ServiceComb-Service-Center](https://github.com/apache/servicecomb-service-center)
  is a Restful based service-registry that provides 
 micro-services discovery and micro-service management. It is based on Open API format 
 and provides features like service-discovery, fault-tolerance, dynamic routing, 
 notify subscription and scalable by design. 
 * [Apache-ServiceComb-Java-Chassis](https://github.com/apache/servicecomb-java-chassis)
  It is a microservice framework based on vert. X and swagger management. 
It adopts the thread model of reactive.
It provides [edge-service](https://support.huaweicloud.com/bestpractice-servicestage/servicestage_bestpractice_0111.html) for gateway, which is better than spring cloud 
gateway and Netflix zuul in performance test.
 * [Apache-ServiceComb-Kie](https://github.com/apache/servicecomb-kie)
  It is a key value based registry, which supports user-defined tags 
and provides version control and regression functions.

## Checking out and building


Requirements：
* Spring Cloud ：2.1.2.RELEASE
* JDK ：1.8 +

do the following:

	git clone https://github.com/huaweicloud/spring-cloud-huawei.git
	cd spring-cloud-huawei
	mvn package  --settings .maven.settings.xml

## How to use
spring-cloud-huawei is released in Huawei cloud open source warehouse.You need to configure the local Maven configuration settings.xml file to set the private server

1.add config in profiles。

    <profile>
        <id>MyProfile</id> 
        <repositories>
            <repository>
                <id>HuaweiCloudSDK</id>
                <url>https://mirrors.huaweicloud.com/repository/maven/huaweicloudsdk/</url>
                <releases>
                    <enabled>true</enabled>
                </releases>
                <snapshots>
                    <enabled>false</enabled>
                </snapshots>
            </repository>
        </repositories>
    </profile>
    
2.add config in mirrors：

    <mirror>
        <id>huaweicloud</id>
        <mirrorOf>*,!HuaweiCloudSDK</mirrorOf>
        <url>https://mirrors.huaweicloud.com/repository/maven/</url>
    </mirror>
    
3.add activeProfiles：

    <activeProfiles>
        <activeProfile>MyProfile</activeProfile>    //跟步骤1中的MyProfile保持一致
    </activeProfiles> 
    
dependencyManagement can be used in projects to manage dependencies.

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
[more document](https://support.huaweicloud.com/devg-servicestage/cse_java_0054.html)

## RoadMap
- [ ] Support WebFlux
- [ ] Integrated edge-service
