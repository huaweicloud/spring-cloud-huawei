# 使用微服务引擎2.0的测试用例

* 参考：https://github.com/apache/servicecomb-service-center/tree/master/ux 安装微服务引擎2.0

* 设置环境变量:
  * CSE_V2_SC: 注册中心的地址
  * CSE_V2_CC: 配置中心的地址

* 依次启动 provider、consumer

* 在配置中心增加如下配置：
  * 应用级配置：consumer.yaml。类型为 yaml。 

```yaml
cse:
  v2:
    test:
      foo: foo
```

  * 自定义配置：priority1.yaml。label信息： public=default 。类型为 yaml。 
```yaml
cse:
  v2:
    test:
      priority: v2
      common: common
```

  * 应用级配置：priority2.yaml。类型为 yaml。 
```yaml
cse:
  v2:
    test:
      priority: v2
```
  * 服务级配置：priority3.yaml，微服务性选择basic-consumer。类型为 yaml。 
```yaml
cse:
  v2:
    test:
      priority: v3
```

  * 应用级配置： cse.v2.test.bar: bar 。 类型为 text。 
  
* 执行 tests-client 里面的集成测试用例 （成功）

* 修改
  * priority1.yaml。label信息： public=default 。类型为 yaml。 
```yaml
cse:
  v2:
    test:
      priority: v4
```

* 执行 tests-client 里面的集成测试用例 （成功） 

* 修改
  * priority3.yaml。label信息： app=cse-v2-test-application,service=basic-consumer,environment=production 。类型为 yaml。 
```yaml
cse:
  v2:
    test:
      priority: v5
```

* 执行 tests-client 里面的集成测试用例 （失败） 

* 修改
  * priority3.yaml。label信息： app=cse-v2-test-application,service=basic-consumer,environment=production 。类型为 yaml。 
```yaml
cse:
  v2:
    test:
      priority: v3
```

* 执行 tests-client 里面的集成测试用例 （成功） 