[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# spring-cloud-huawei

[查看中文文档](./README_zh.md)

spring-cloud-huawei is a framework that makes it easier to integrate spring cloud and Huawei frameworks.
Including open source framework and commercial framework, 
open source such as 
[Apache ServiceComb](http://servicecomb.apache.org), 
commercial such as Huawei Cloud 
[ServiceStage](https://www.huaweicloud.com/product/servicestage.html).
## Modules

 * **spring-cloud-starter-huawei-servicecomb-discovery:**
     * Support for use HuaweiCloud CSE/[ServiceComb-Service-Center](https://github.com/apache/servicecomb-service-center)
   :is a Restful based service-registry that provides 
   micro-services discovery and micro-service management. It is based on Open API format 
   and provides features like service-discovery, fault-tolerance, dynamic routing, 
   notify subscription and scalable by design.
   Support multi-environment, multi-dimensional management and multi-registry configuration

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

## Checking out and building


Requirements：
* Spring Cloud ：2.1.2.RELEASE
* JDK ：1.8 +

do the following:

	git clone https://github.com/huaweicloud/spring-cloud-huawei.git
	cd spring-cloud-huawei
	mvn install  --settings .maven.settings.xml

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
