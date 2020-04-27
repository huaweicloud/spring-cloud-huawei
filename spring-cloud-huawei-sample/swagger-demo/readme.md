### 异构应用指南

本demo使用java chassis、go chassis 、spring cloud huawei 展示异构应用的场景下
，采用不同框架开发的微服务如何实现互相调用。

启动三个服务,调用接口：http://127.0.0.1:8081/longCall 实现长调用

调用链路：

    用户(调用者) -> spring cloud -> java chassis -> spring cloud
                                |               |-> go chassis
                                |
                                 -> go chassis -> spring cloud
                                               |-> java chassis

在实际使用场景中，由于java chassis支持的契约、数据格式等为spring cloud的子集，有一些问题需要注意：
1. java chassis调用spring cloud时，只需要确保spring cloud生成的契约格式java chassis可以识别；而在数据格式方面，spring cloud理论上支持所有的java chassis的数据格式。
2. spring cloud调用java chassis时，由于spring cloud不依赖于契约，所以只需要确保spring cloud的数据格式为json才可以被java chassis识别。
3. 在使用java chassis/go chassis调用其他微服务应用时url的前缀应为cse://；在使用spring cloud调用其他微服务应用时url的前缀应为http://。
4. java chassis返回字符串时会把字符串包装成json，而spring cloud和go chassis则会返回text不会包装。(包装成json的字符串会被双引号包括)
5. java chassis调用go chassis注意要在go chassis的Route中手动声明相关的Parameters(request param)、Read(request body)。

开发spring cloud时：
- request param 不应包含 object ，否则启动时会异常提示。
- 接口的http method必须指定，契约才可以注册。如果Controller中存在没有指定method的接口，会打印warn日志。
- 对于含有request body的请求，确保ContentType为application/json。

如有其他问题，欢迎发起issue讨论解决。