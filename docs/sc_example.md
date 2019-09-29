## Run Demo
Use the examples provided by the project to implement registration and discovery.
We provide three projects under spring-cloud-huawei-sample/discovery-demo to simulate 
the migration of the spring cloud project to the Apache ServiceComb registry:

* price-provider: The price service is a provider service 
that provides an interface in a RESTful way.
* order-consumer: The order service is a service consumer. 
Call the price-provider to get price information through **RestTemplate**.
* product-consumer-feign: The product service is a service consumer. 
Call the price-provider to get price information through **Feign**.


do the following:


1.download [servicecomb-service-center](https://github.com/apache/servicecomb-service-center/releases)， 
Reference [servicecomb-service-center quick start](https://github.com/apache/servicecomb-service-center#quick-start)
try to run servicecomb-service-center

2.Start three sample services
View the service status through the actuator
  `http://127.0.0.1:8080/actuator/service-registry`
  
If successful, it displays UP

If you want to set the status of the service, you can:

`curl -i -H "Content-Type: application/json" -X POST -d '{"status":"DOWN"}' http://localhost:8080/actuator/service-registry`

Change the status of the service to DOWN.

3.View the servicecomb-service-center,visit http://127.0.0.1:30100

check status of instance.

4.Verify the result of call using curl, browser, or postman,

product-consumer-feign：
  `http://127.0.0.1:8089/product?id=11`

order-consume：
  `http://127.0.0.1:8088/order?id=11`