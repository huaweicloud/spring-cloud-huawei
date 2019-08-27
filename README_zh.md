[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
# spring-cloud-huawei

[English document](https://github.com/huaweicloud/spring-cloud-huawei)

此框架的目的是为了让spring cloud 和华为的框架更好的融合在一起。
包括开源的框架和商业的框架，开源的如[Apache ServiceComb](http://servicecomb.apache.org)
，商业的如华为云[ServiceStage](https://www.huaweicloud.com/product/servicestage.html)。
## 功能特性

- [x] **Spring Cloud 使用 ServiceComb-Service-Center 实现注册和发现:**
大部分情况无需修改代码，只需要修改配置文件(application.yml)即可实现对接。

- [x] **注册中心高可用、负载均衡：**
可以启动多个注册中心，客户端会选择其中一个健康的注册中心进行调用。

## 构建代码

依赖的版本：
* Spring Cloud ：2.1.2.RELEASE
* JDK ：1.8 +

运行如下命令:

	git clone https://github.com/huaweicloud/spring-cloud-huawei.git
	cd spring-cloud-huawei
	mvn package

## 开发路径
- [ ] 分布式事务DTM整合
- [ ] 兼容ServiceStage的仪表盘