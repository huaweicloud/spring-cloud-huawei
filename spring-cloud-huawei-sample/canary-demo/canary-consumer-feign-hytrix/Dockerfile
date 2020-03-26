FROM openjdk:8u181-jdk-alpine

WORKDIR /home/apps/
ADD target/canary-consumer-feign-hytrix-1.2.0.jar .
ADD target/lib ./lib
ADD start.sh .

ENTRYPOINT ["sh", "/home/apps/start.sh"]