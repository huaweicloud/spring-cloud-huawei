FROM openjdk:8u181-jdk-alpine

WORKDIR /home/apps/
ADD target/canary-provider-beta-1.3.1-Greenwich.jar .
ADD target/lib ./lib
ADD start.sh .

ENTRYPOINT ["sh", "/home/apps/start.sh"]