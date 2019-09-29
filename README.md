[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
# spring-cloud-huawei

[查看中文文档](https://github.com/huaweicloud/spring-cloud-huawei/blob/master/README_zh.md)

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

## Components

 * [Apache-ServiceComb-Service-Center](https://github.com/apache/servicecomb-service-center)
  is a Restful based service-registry that provides 
 micro-services discovery and micro-service management. It is based on Open API format 
 and provides features like service-discovery, fault-tolerance, dynamic routing, 
 notify subscription and scalable by design. 


## Checking out and building


Requirements：
* Spring Cloud ：2.1.2.RELEASE
* JDK ：1.8 +

do the following:

	git clone https://github.com/huaweicloud/spring-cloud-huawei.git
	cd spring-cloud-huawei
	mvn package

## How to use
Because spring-cloud-huawei has not been released to the public repository, if you want to use it, you need to download the code to build locally.
    
    mvn clean install

dependencyManagement can be used in projects to manage dependencies.

    <dependencyManagement>
      <dependencies>
        <dependency>
          <groupId>org.springframework.cloud.huawei</groupId>
          <artifactId>spring-cloud-huawei-dependencies</artifactId>
          <version>${project.version}</version>
          <type>pom</type>
          <scope>import</scope>
        </dependency>
      </dependencies>
    </dependencyManagement>
[more document](https://github.com/huaweicloud/spring-cloud-huawei/blob/master/docs/index.md)

## RoadMap
- [ ] Integrated distributed transaction DTM
- [ ] Integrated Config-Center
- [ ] integrated Gray Release
