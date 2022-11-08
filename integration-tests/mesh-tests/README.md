# integration tests for mesh

Applications run in docker:

* mesh-gateway --> mesh-consumer --> mesh-provider

Application run in host:

* mesh-tests-client

## Note on run local IDE

Configure hosts (e.g. /etc/hosts or C:\Windows\System32\drivers\etc\hosts) :

```text
mesh-provider-test 127.0.0.1
mesh-consumer-test 127.0.0.1
```

When running in docker, `mesh-provider-test` and `mesh-consumer-test` is resolved as service

When running in Istio, should change host to the real proxy host, e.g. 127.0.0.1. 

```yaml
    # 启用 Mesh 注册发现
    discovery:
      client:
        mesh:
          enabled: true
          host: mesh-consumer-test # 模拟 proxy 地址
          port: 2992
          secure: false
          metadata:
            tag: mesh-gateway
```
