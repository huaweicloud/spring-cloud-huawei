
## Configuration(application.yaml) instructions

<table class="table-bordered table-striped table-condensed">
   <tr>
      <th>Configuration</th>
      <th>Key</th>
      <th>Default Value</th>
   </tr>
   <tr>
      <td>Enable ServiceComb discovery</td>
      <td>spring.cloud.servicecomb.discovery.enabled</td>
      <td>true</td>
   </tr>
    <tr>
       <td>Registry address</td>
       <td>spring.cloud.servicecomb.discovery.address</td>
       <td></td>
    </tr>
    <tr>
       <td>Service name</td>
       <td>spring.cloud.servicecomb.discovery.serviceName</td>
       <td>use spring.application.name if no spring.cloud.servicecomb.serviceName</td>
    </tr>
    <tr>
       <td>Appliaction name</td>
       <td>spring.cloud.servicecomb.discovery.appName</td>
       <td>default</td>
    </tr>
    <tr>
       <td>version</td>
       <td>spring.cloud.servicecomb.discovery.version</td>
       <td></td>
    </tr>
    <tr>
       <td>Enable healthCheck</td>
       <td>spring.cloud.servicecomb.discovery.healthCheck</td>
       <td>true</td>
    </tr>
    <tr>
       <td>HealthCheck Interval</td>
       <td>spring.cloud.servicecomb.discovery.healthCheckInterval</td>
       <td>10s</td>
    </tr>
    <tr>
       <td>Auto discovery Service-Center</td>
       <td>spring.cloud.servicecomb.discovery.autoDiscovery</td>
       <td>false</td>
    </tr>

</table>