FROM openjdk:8u181-jdk-alpine

WORKDIR /home/apps/
ADD target/canary-consumer-1.3.2.jar .
ADD target/lib ./lib
ADD start.sh .

ENTRYPOINT ["sh", "/home/apps/start.sh"]