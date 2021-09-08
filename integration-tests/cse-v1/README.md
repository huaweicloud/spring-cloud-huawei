# 使用微服务引擎专业版的测试用例

* 首先登陆华为云华南区，获取 AK/SK。 测试机器设置环境变量： CREDENTIALS_AK、CREDENTIALS_SK。
* 在配置中心增加如下配置：
  * greenwich-consumer.yaml

```yaml
cse.v1.test.foo: foo
cse.v1.test.sequences[0]: s0
cse.v1.test.sequences[1]: s1
cse.v1.test.configModels[0].name: s1
cse.v1.test.configModels[0].index: 2
cse.v1.test.configModels[1].name: s2
cse.v1.test.configModels[1].index: 3
```

* 依次启动 provider、consumer、gateway
* 在配置中心增加如下配置：
  * cse.v1.test.bar: bar

* 执行 tests-client 里面的集成测试用例
