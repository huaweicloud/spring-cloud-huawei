FROM openjdk:8u181-jdk-alpine

WORKDIR /home/apps/
ADD ../../spring-cloud-huawei-sample/canary-demo/canary-procvider/target/canary-provider-0.0.1.jar .
ADD ../../spring-cloud-huawei-sample/canary-demo/canary-procvider/target/lib ./lib
ADD start.sh .

ENTRYPOINT ["sh", "/home/apps/start.sh"]