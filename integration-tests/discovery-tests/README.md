# 集成测试说明

本集成测试用例开发了两个微服务：

- order-consumer: 监听 8088 端口
- price-provider: 监听 8080 端口

discovery-tests-client是集成测试用例， 会在 docker 中先启动 order-consumer 和 
price-provider，然后运行各个集成测试用例。 

