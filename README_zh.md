[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# spring-cloud-huawei

[English document](https://github.com/huaweicloud/spring-cloud-huawei)

此框架的目的是为了让spring cloud 和华为的框架更好的融合在一起。
包括开源的框架和商业的框架，开源的如[Apache ServiceComb](http://servicecomb.apache.org)
，商业的如华为云[ServiceStage](https://www.huaweicloud.com/product/servicestage.html)。
## 功能特性
### 开源
- [x] **Spring Cloud 使用 ServiceComb-Service-Center 实现注册和发现:**
只需要修改配置文件(application.yml)即可实现对接。
可以启动多个注册中心，客户端会选择其中一个健康的注册中心进行调用。

- [x] **Spring Cloud 使用ServiceStage灰度发布服务：**
支持灰度发布能力。

- [x] **Spring Cloud 支持生成swagger契约接口信息 :**
无需配置即可生成swagger契约并注册到 ServiceComb server center注册中心。

- [x] **Spring Cloud 与 ServiceComb Java-Chassis / Edge-Service 应用组网:**
支持与微服务框架ServiceComb Java-Chassis组网，使用ServiceComb Edge-Service网关，体验reactive带来的性能提升。

- [x] **Spring Cloud 使用 ServiceComb-Kie。 :**
支持从ServiceComb-Kie获取配置，动态更新。

### 华为云

- [x] **Spring Cloud 使用ServiceStage注册中心实现注册发现：**
只需要修改配置文件(application.yml)即可实现对接。

- [x] **Spring Cloud 使用ServiceStage分布式事务DTM：**
（Distributed Transaction Management，DTM）是一款用于解决分布式环境下事务一致性问题的产品。

- [x] **Spring Cloud 使用ServiceStage分布式配置服务：**
支持从华为云微服务引擎服务端获取配置，动态更新。

## 组件

 * [Apache-ServiceComb-Service-Center](https://github.com/apache/servicecomb-service-center)
  是一个基于Restful的提供微服务发现和微服务治理的服务注册中心。
  它基于Open API规范并提供服务发现、容错、动态路由、订阅和可扩展设计等功能。
 * [Apache-ServiceComb-Java-Chassis](https://github.com/apache/servicecomb-java-chassis)
  是一个基于Vert.x和swagger管理的微服务框架，采用Reactive的线程模型。
  提供网关[Edge-Service](https://support.huaweicloud.com/bestpractice-servicestage/servicestage_bestpractice_0111.html)，在性能测试中性能优于spring cloud gateway和netflix zuul。
 * [Apache-ServiceComb-Kie](https://github.com/apache/servicecomb-kie)
  是一个基于key value的注册中心，支持自定义标签，提供版本控制、回归功能。

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

