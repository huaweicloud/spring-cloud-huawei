## 运行demo
通过项目中的示例开始分布式事务。spring-cloud-huawei-sample/dtm-demo下提供了三个工程，用来模拟spring cloud 
工程使用华为云ServiceStage分布式事务的场景：

* reserve：预订服务使用RestTemplate，事务发起方。
* coupon：优惠券服务，分支事务。
* ticket：票务服务，分支事务。
