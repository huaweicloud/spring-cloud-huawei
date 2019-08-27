
## 配置说明(application.yaml)

<table class="table-bordered table-striped table-condensed">
   <tr>
      <th>配置</th>
      <th>Key</th>
      <th>默认值</th>
   </tr>
   <tr>
      <td>启动ServiceComb服务发现</td>
      <td>spring.cloud.servicecomb.discovery.enabled</td>
      <td>true</td>
   </tr>
    <tr>
       <td>注册中心地址</td>
       <td>spring.cloud.servicecomb.discovery.address</td>
       <td></td>
    </tr>
    <tr>
       <td>服务名</td>
       <td>spring.cloud.servicecomb.discovery.serviceName</td>
       <td></td>
    </tr>
    <tr>
       <td>应用名</td>
       <td>spring.cloud.servicecomb.discovery.appName</td>
       <td>default</td>
    </tr>
    <tr>
       <td>版本号</td>
       <td>spring.cloud.servicecomb.discovery.version</td>
       <td></td>
    </tr>
    <tr>
       <td>启动健康检查</td>
       <td>spring.cloud.servicecomb.discovery.healthCheck</td>
       <td>true</td>
    </tr>
    <tr>
       <td>健康检查间隔时间</td>
       <td>spring.cloud.servicecomb.discovery.healthCheckInterval</td>
       <td>10s</td>
    </tr>
    <tr>
       <td>自动发现注册中心集群地址，
       如果只配置一个，
       可以发现健康的注册中心</td>
       <td>spring.cloud.servicecomb.discovery.autoDiscovery</td>
       <td>false</td>
    </tr>

</table>