FROM openjdk:8u181-jdk-alpine

WORKDIR /home/apps/
ADD ../../spring-cloud-huawei-sample/canary-demo/canary-consumer-feign-hytrix/target/canary-consumer-feign-hytrix-0.0.1.jar .
ADD ../../spring-cloud-huawei-sample/canary-demo/canary-consumer-feign-hytrix/target/lib ./lib
ADD start.sh .

ENTRYPOINT ["sh", "/home/apps/start.sh"]