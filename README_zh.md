[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
# spring-cloud-huawei

[English document](https://github.com/huaweicloud/spring-cloud-huawei)

此框架的目的是为了让spring cloud 和华为的框架更好的融合在一起。
## 功能特性

* **Spring Cloud 使用 ServiceComb-Service-Center 实现注册和发现:**
大部分情况无需修改代码，只需要修改配置文件(application.yml)即可实现对接。

* **注册中心高可用、负载均衡：**
可以启动多个注册中心，客户端会选择其中一个健康的注册中心进行调用。

## 构建代码
运行如下命令:

	git clone https://github.com/huaweicloud/spring-cloud-huawei.git
	cd spring-cloud-huawei
	mvn package


## 快速开始
通过项目中的示例开始实现服务注册发现。spring-cloud-servicecomb-sample下提供了三个工程，用来模拟spring cloud 
工程迁移到Apache ServiceComb 的注册中心的场景：

* price-provider：价格服务，服务提供者，RESTful方式提供接口。
* order-consumer：订单服务，服务消费者，RESTful方式提供接口，以RestTemplate的方式调用price-provider获取价格信息。
* product-consumer-feign：产品服务，服务消费者，RESTful方式提供接口，以feign的方式调用price-provider获取价格信息。


按照如下步骤进行：

1.下载 [servicecomb-service-center](https://github.com/apache/servicecomb-service-center/releases)， 
参考[servicecomb-service-center的quick start文档](https://github.com/apache/servicecomb-service-center#quick-start)
运行 servicecomb-service-center

2.分别运行main函数，启动三个示例服务

通过actuator查看服务运行情况
  `http://127.0.0.1:8080/actuator/service-registry`
  
正确情况会显示 UP

如果要设置服务的状态，可以通过：

`curl -i -H "Content-Type: application/json" -X POST -d '{"status":"DOWN"}' http://localhost:8080/actuator/service-registry`

将服务的状态修改为DOWN。

3.查看服务注册中心http://127.0.0.1:30100

查看实例运行状态。

4.curl、浏览器或使用postman验证调用情况，

product-consumer-feign访问地址：
  `http://127.0.0.1:8089/product?id=11`

order-consume访问地址：
  `http://127.0.0.1:8088/order?id=11`

## 配置说明

[查看文档](https://github.com/huaweicloud/spring-cloud-huawei/blob/master/doc/configuration_zh.md)

## 开发路径
* 分布式事务DTM整合
