FROM openjdk:8u181-jdk-alpine

WORKDIR /home/apps/
ADD target/canary-provider-0.0.3.jar .
ADD target/lib ./lib
ADD start.sh .

ENTRYPOINT ["sh", "/home/apps/start.sh"]