
## 配置说明(application.yaml)

| 配置                                                         | Key                                                    | 默认值                                   |
| ------------------------------------------------------------ | ------------------------------------------------------ | ---------------------------------------- |
| 启动ServiceComb服务发现                                      | spring.cloud.servicecomb.discovery.enabled             | true                                     |
| 注册中心地址                                                 | spring.cloud.servicecomb.discovery.address             |                                          |
| 服务名                                                       | spring.cloud.servicecomb.discovery.serviceName         | 如果没有，使用spring.application.name    |
| 应用名                                                       | spring.cloud.servicecomb.discovery.appName             | default                                  |
| 版本号                                                       | spring.cloud.servicecomb.discovery.version             | [0.0.0+](sc_configuration_version_zh.md) |
| 启动健康检查                                                 | spring.cloud.servicecomb.discovery.healthCheck         | true                                     |
| 健康检查间隔时间                                             | spring.cloud.servicecomb.discovery.healthCheckInterval | 10s                                      |
| 自动发现注册中心集群地址， 如果只配置一个， 可以发现健康的注册中心 | spring.cloud.servicecomb.discovery.autoDiscovery       | false                                    |