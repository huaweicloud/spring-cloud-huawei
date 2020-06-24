# 集成测试说明

本集成测试用例开发了两个微服务：

- order-consumer: 监听 8088 端口，docker 运行
- price-provider: 监听 8080 端口，docker 运行

- discovery-test-client: 集成测试用例，宿主机运行。

集成测试自动运行： maven 插件先在 docker 中启动 order-consumer 和 
price-provider，然后运行discovery-test-client的集成测试用例。

本地手工运行：在 IDEA 中依次启动 order-consumer 和 
price-provider, 然后运行discovery-test-client的集成测试用例。
