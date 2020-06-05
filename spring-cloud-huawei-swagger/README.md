# 契约注册模块

本模块的主要功能是扫描所有的 `@RestController`， 生成 swagger， 然后将 swagger 内容注册到服务中心。

swagger 生成有两种模式：

* spring cloud 原生模式
  
  spring cloud 原生模式根据 spring 接口定义的规则生成 swagger， 不进行任何处理。 spring cloud
  的接口定义非常灵活，比如一个接口可以对应多个 Path， 可以对应多个 Method。 

* 适配 servicecomb-java-chassis 模式

  这种模式在 spring cloud 原始模式生成的 swagger 基础之上，对 swagger 进行裁剪， 以使得 spring cloud
  应用能够更好的和 servicecomb-java-chassis 微服务协作。 servicecomb-java-chassis 在 REST 接口
  定义方面更加规范， 比如一个接口只能对应一个 Path， 只能对应一个 Method， 存在唯一的 operation id 等。

默认启用适配 servicecomb-java-chassis 模式， 可以通过下面的配置开关进行切换：

```yaml
spring.cloud.servicecomb.swagger.enableJavaChassisAdapter: false
```

## 详细的裁剪策略

* `@RequestMapping` 裁剪，等价于 `@GetMapping`

  spring cloud `@RequestMapping` 声明的接口，既可以使用 `Get` 访问， 也可以使用 `Post` 访问，
  注册契约的时候，裁剪为只注册 `Get` 访问。 

其他详细裁剪细节可以参考 `ServiceCombDocumentationSwaggerMapper` 的实现。