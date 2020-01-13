[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# spring-cloud-huawei

[English document](https://github.com/huaweicloud/spring-cloud-huawei)

此框架的目的是为了让spring cloud 和华为的框架更好的融合在一起。
包括开源的框架和商业的框架，开源的如[Apache ServiceComb](http://servicecomb.apache.org)
，商业的如华为云[ServiceStage](https://www.huaweicloud.com/product/servicestage.html)。
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
     * 基于代码零配置自动生成swagger接口契约，自动注册到Service-Center注册中心进行接口文档化管理。
     * 基于契约与微服务框架[ServiceComb-Java-Chassis](https://github.com/apache/servicecomb-java-chassis)组网。
     * 使用[Edge-Service](https://support.huaweicloud.com/bestpractice-servicestage/servicestage_bestpractice_0111.html)网关，
 [表现](https://github.com/AngLi2/api-gateway-benchmark/blob/master/Spring%20Cloud%20Gateway%2C%20Zuul%2C%20Edge%20Service%20%E6%80%A7%E8%83%BD%E5%AF%B9%E6%AF%94.md)
 优于spring cloud gateway和netflix zuul，体验reactive带来的性能提升。

## 构建代码

依赖的版本：
* Spring Cloud ：2.1.2.RELEASE
* JDK ：1.8 +

运行如下命令:

	git clone https://github.com/huaweicloud/spring-cloud-huawei.git
	cd spring-cloud-huawei
	mvn install  --settings .maven.settings.xml

## 如何使用
spring-cloud-huawei发布在华为云开源仓库，需要配置本地maven配置settings.xml文件设置私服。
    
1.profiles中增加如下配置。

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
    
2.在mirrors节点中增加：

    <mirror>
        <id>huaweicloud</id>
        <mirrorOf>*,!HuaweiCloudSDK</mirrorOf>
        <url>https://mirrors.huaweicloud.com/repository/maven/</url>
    </mirror>
    
3.新增activeProfiles配置：

    <activeProfiles>
        <activeProfile>MyProfile</activeProfile>    //跟步骤1中的MyProfile保持一致
    </activeProfiles> 
    
项目中可以使用dependencyManagement引入依赖。

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
    
[更多文档](https://support.huaweicloud.com/devg-servicestage/cse_java_0054.html)

## 开发路径
- [ ] 支持WebFlux
- [ ] 集成servicecomb的APIGateway

