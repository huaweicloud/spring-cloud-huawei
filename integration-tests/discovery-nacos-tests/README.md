[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.huaweicloud/spring-cloud-huawei/badge.svg)](https://search.maven.org/search?q=g:com.huaweicloud%20AND%20a:spring-cloud-huawei-dependencies) 
# 集成测试说明

本集成测试用例开发了三个微服务：

- price-provider:   监听 9090 端口，docker 运行。 
- account-provider: 监听 9092 端口，docker 运行。 appName: account-app，测试跨应用调用。
- order-consumer:   监听 9098 端口，docker 运行。
- gateway:          监听 10088 端口，docker 运行。

- discovery-test-client: 集成测试用例，宿主机运行。

集成测试自动运行： maven 插件先在 docker 中启动 gateway, account-provider、order-consumer 和 
price-provider，然后运行discovery-test-client的集成测试用例。

本地手工运行：在 IDEA 中依次启动 gateway, account-provider、order-consumer 和 
price-provider, 然后运行discovery-test-client的集成测试用例。


# Nacos 和 ServiceComb 功能差异

| 功能项             |ServiceComb|Nacos|备注|
|-----------------|---|---|---|
| 契约生成和注册         |支持|不支持||
| 基于注册中心的服务之间公钥认证 |支持|不支持||
|跨应用（分组）访问|支持|不支持||
|实例服务元数据分离|支持|不支持|Nacos只支持服务元数据|
|灰度发布功能|支持|不支持|Nacos没有服务版本等概念，或者需要适配|
|仪表盘监控信息上报|支持|不支持|

