[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
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

### 华为云

- [x] **Spring Cloud 使用ServiceStage注册中心实现注册发现：**
只需要修改配置文件(application.yml)即可实现对接。

- [x] **Spring Cloud 使用ServiceStage分布式事务DTM：**
（Distributed Transaction Management，DTM）是一款用于解决分布式环境下事务一致性问题的产品。

- [x] **Spring Cloud 使用ServiceStage分布式配置服务：**
支持从服务端获取配置，并且动态更新，遵循Spring Cloud的用法，可以无缝对接。

- [x] **Spring Cloud 使用ServiceStage灰度发布服务：**
支持灰度发布能力。
## 组件

 * [Apache-ServiceComb-Service-Center](https://github.com/apache/servicecomb-service-center)
  是一个基于Restful的提供微服务发现和微服务治理的服务注册中心。
  它基于Open API规范并提供服务发现、容错、动态路由、订阅和可扩展设计等功能。

## 构建代码

依赖的版本：
* Spring Cloud ：2.1.2.RELEASE
* JDK ：1.8 +

运行如下命令:

	git clone https://github.com/huaweicloud/spring-cloud-huawei.git
	cd spring-cloud-huawei
	mvn package  --settings .maven.settings.xml

## 如何使用
因为spring-cloud-huawei还没有发布到公共仓库，如果要使用，需要先下载代码在本地构建。
    
    mvn clean install --settings .maven.settings.xml

项目中可以使用dependencyManagement引入依赖。

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
[更多文档](https://github.com/huaweicloud/spring-cloud-huawei/blob/master/docs/index.md)

## 开发路径
- [ ] 集成openapi-swagger2.0
- [ ] 支持WebFlux
- [ ] 集成serivicecomb-kie
- [ ] 集成servicecomb的APIGateway

