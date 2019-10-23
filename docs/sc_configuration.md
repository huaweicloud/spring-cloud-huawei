
## Configuration(application.yaml) instructions

| Configuration                 | Key                                                    | Default Value                                                |
| ----------------------------- | ------------------------------------------------------ | ------------------------------------------------------------ |
| Enable ServiceComb discovery  | spring.cloud.servicecomb.discovery.enabled             | true                                                         |
| Registry address              | spring.cloud.servicecomb.discovery.address             |                                                              |
| Service name                  | spring.cloud.servicecomb.discovery.serviceName         | use spring.application.name if no spring.cloud.servicecomb.serviceName |
| Appliaction name              | spring.cloud.servicecomb.discovery.appName             | default                                                      |
| version                       | spring.cloud.servicecomb.discovery.version             |  [0.0.0+](sc_configuration_version.md)                       |
| Enable healthCheck            | spring.cloud.servicecomb.discovery.healthCheck         | true                                                         |
| HealthCheck Interval          | spring.cloud.servicecomb.discovery.healthCheckInterval | 10s                                                          |
| Auto discovery Service-Center | spring.cloud.servicecomb.discovery.autoDiscovery       | false                                                        |