### 跨语言调用链跟踪

本demo提供一个案例，以一个基于spring cloud huawei的程序作为消费端，
一个go chassis的程序作为消费端，他们同时接入zipkin并进行调用，利用zipkin收集跨语言的调用链信息。

Zipkin是一个开源的分布式跟踪系统，项目托管在：https://github.com/openzipkin/zipkin
可以根据以下地址下载zipkin的最新jar包：https://dl.bintray.com/openzipkin/maven/io/zipkin/java/zipkin-server/
推荐下载zipkin-server-2.12.9-exec.jar

#### 本地运行

1. 运行java -jar zipkin-server-2.12.9-exec.jar命令启动zipkin，zipkin默认监听9411端口，在http://127.0.0.1:9411/即可访问console页面。
2. 本地运行go-provider程序和spring-consumer程序。
3. 访问http://localhost:8081/hello接口，调用路径：用户 -> spring-consumer -> go-provider，得到返回结果hello表示调用成功。
4. 访问zipkin界面，刷新，可以看到服务调用链记录。

#### 部署在servicestage上

1. 进入spring-consumer项目内，运行mvn package打包，在/target/spring-consumer-1.0-SNAPSHOT.jar可得到可执行jar包。
2. 将spring-consumer-1.0-SNAPSHOT.jar 和 zipkin-server-2.12.9-exec.jar上传到servicestage的软件中心上。
3. 通过在servicestage中创建应用组件，部署两个服务，参考官网[资料](https://support.huaweicloud.com/usermanual-servicestage/servicestage_user_0409.html)。
4. go-provider的部署方式稍有不同，采用docker部署，demo中提供了dockerfile文件，可以[参考资料](https://support.huaweicloud.com/qs-servicestage/servicestage_qs_0035.html)
   中的部署部分进行开发部署。
5. 部署时要保证三个服务在同一vpc下，优先部署zipkin服务。部署完后zipkin服务后设置环境变量 1. ssAddr：设置为微服务引擎的地址。2. zipkinAddr：设置为 http://{zipkin服务的vpc内地址}:9411。
   环境变量设置完成后部署spring-consumer和go-provider两个服务。
6. 为zipkin服务[绑定公网IP](https://support.huaweicloud.com/usermanual-servicestage/servicestage_user_0500.html)，绑定公网IP的目的是能访问到zipkin的console界面，并方便在本地进行服务的调用测试。
7. 参考上一个章节[本地运行]，进行服务调用，并观察zipkin的console界面，可以观察到服务调用链记录。