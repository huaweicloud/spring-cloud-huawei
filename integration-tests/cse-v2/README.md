# 使用微服务引擎2.0的测试用例

* 参考：https://github.com/apache/servicecomb-service-center/tree/master/ux 安装微服务引擎2.0

* 在配置中心增加如下配置：
  * consumer.yaml。 label信息： app=cse-v2-test-application,environment=production 。类型为 yaml。 

```yaml
cse:
  v2:
    test:
      foo: foo
```
      
* 设置环境变量:
  * CSE_V2_SC: 注册中心的地址
  * CSE_V2_CC: 配置中心的地址

* 依次启动 provider、consumer、gateway

* 在配置中心增加如下配置：
  * cse.v2.test.bar: bar
  * label信息： app=cse-v2-test-application,environment=production 。类型为 text。 
  
* 执行 tests-client 里面的集成测试用例  
