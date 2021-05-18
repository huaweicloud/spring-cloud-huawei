# 使用微服务引擎2.0并启用RBAC功能的测试用例

* 首先登陆华为云华南区，获取 AK/SK。 测试机器设置环境变量： CREDENTIALS_AK、CREDENTIALS_SK。
* 在配置中心增加如下配置：
  * consumer.yaml

```yaml
cse:
  v1:
    test:
      foo: foo
```

* 使用环境变量设置RBAC信息：
  * rbac_account_name: 账户名称
  * rbac_password:  账户密码
    
* 使用环境变量替换以下配置
  * spring.cloud.servicecomb.discovery.address: ${your_service_center_address}
  * spring.cloud.servicecomb.config.serverAddr: ${your_config_center_address}

* 依次启动 provider、consumer、gateway

* 在配置中心增加如下配置：
  * cse.v2.test.bar: bar
  
* 执行 tests-client 里面的集成测试用例  
