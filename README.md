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

- [x] **Spring Cloud use ServiceComb-Service-Center to implement registration and discovery:**
No need to change the code, just modify the individual configuration files(application.yml) to complete the migration.

- [x] **Service-Center is highly available, load balanced:**
Multiple Service-Centers can be launched and the client will select one of the healthy Service-Centers to make the call.

- [ ] 
## Checking out and building


Requirements：
* Spring Cloud ：2.1.2.RELEASE
* JDK ：1.8 +

do the following:

	git clone https://github.com/huaweicloud/spring-cloud-huawei.git
	cd spring-cloud-huawei
	mvn package

## RoadMap
- [ ] Integrated distributed transaction DTM
- [ ] Integrated Dashboard of ServiceStage 