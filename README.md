[![Build Status](https://travis-ci.org/huaweicloud/spring-cloud-huawei.svg?branch=master)](https://travis-ci.org/huaweicloud/spring-cloud-huawei)
[![Coverage Status](https://coveralls.io/repos/github/huaweicloud/spring-cloud-huawei/badge.svg?branch=master)](https://coveralls.io/github/huaweicloud/spring-cloud-huawei?branch=master)
# spring-cloud-huawei

[查看中文文档](https://github.com/huaweicloud/spring-cloud-huawei/blob/master/README_zh.md)

This is a library, in order to make the spring cloud and huawei framework very easy to be compatible.

## Features

* **Spring Cloud use ServiceComb-Service-Center to implement registration and discovery:**
No need to change the code, just modify the individual configuration files(application.yml) to complete the migration.

* **Service-Center is highly available, load balanced:**
Multiple Service-Centers can be launched and the client will select one of the healthy Service-Centers to make the call.

## Checking out and building
do the following:

	git clone https://github.com/huaweicloud/spring-cloud-huawei.git
	cd spring-cloud-huawei
	mvn package


## Quick Start
Use the examples provided by the project to implement registration and discovery.
We provide three projects under spring-cloud-servicecomb-sample to simulate 
the migration of the spring cloud project to the Apache ServiceComb registry:

* price-provider: The price service is a provider service 
that provides an interface in a RESTful way.
* order-consumer: The order service is a service consumer. 
Call the price-provider to get price information through **RestTemplate**.
* product-consumer-feign: The product service is a service consumer. 
Call the price-provider to get price information through **Feign**.


do the following:


1.download [servicecomb-service-center](https://github.com/apache/servicecomb-service-center/releases)， 
Reference [servicecomb-service-center quick start](https://github.com/apache/servicecomb-service-center#quick-start)
try to run servicecomb-service-center

2.Start three sample services
View the service status through the actuator
  `http://127.0.0.1:8080/actuator/service-registry`
  
If successful, it displays UP

If you want to set the status of the service, you can:

`curl -i -H "Content-Type: application/json" -X POST -d '{"status":"DOWN"}' http://localhost:8080/actuator/service-registry`

Change the status of the service to DOWN.

3.View the servicecomb-service-center,visit http://127.0.0.1:30100

check status of instance.

4.Verify the result of call using curl, browser, or postman,

product-consumer-feign：
  `http://127.0.0.1:8089/product?id=11`

order-consume：
  `http://127.0.0.1:8088/order?id=11`

## Configuration(application.yaml) instructions

[document](https://github.com/huaweicloud/spring-cloud-huawei/blob/master/doc/configuration.md)


## RoadMap
* Integrated distributed transaction DTM
* Integrated Dashboard of ServiceStage 