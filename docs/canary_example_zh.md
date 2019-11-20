## 运行demo
通过项目中的示例开始实现灰度发布。

通过项目中的示例开始灰度发布。spring-cloud-huawei-sample/canary-demo下提供了4个工程，用来模拟spring cloud 
工程使用华为云ServiceStage分布式事务的场景：

* canary-consumer：模拟服务消费方。
* canary-consumer-feign-hytrix：模拟服务消费方，使用feign客户端并集成hytrix的场景。
* canary-provider：模拟服务提供者。
* canary-provider-beta：模拟服务提供者，灰度版本。


## 在华为云上运行Demo

### 前提条件

已经在[huaweicloud](https://www.huaweicloud.com)上开通账号，并且在[ServiceStage](https://www.huaweicloud.com/product/servicestage.html)上添加了微服务引擎和分布式事务引擎。

### step 1 构建工程

因为spring-cloud-huawei还没有发布到公共仓库，如果要使用，需要先下载代码在本地构建。
    
    mvn clean install --settings .maven.settings.xml

### step 2 分别3个工程运行main函数

启动canary-consumer 消费者 和 canary-provider， canary-provider-beta两个服务提供者

按照[规则](https://docs.go-chassis.com/user-guides/router.html)调整配置文件。

### step 3 调用接口

http://localhost:8091/canary?id=123   并设置响应的header信息
 
观察响应信息，已经按照配置的规则进行预期的流量分配。

>支持客户端使用feign和hytrix的场景，配置方法相同，详情见canary-consumer-feign-hytrix