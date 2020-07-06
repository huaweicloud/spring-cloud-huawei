FROM openjdk:8u181-jdk-alpine

WORKDIR /home/apps/
ADD target/canary-provider-1.3.3-Hoxton-SNAPSHOT.jar .
ADD target/lib ./lib
ADD start.sh .

ENTRYPOINT ["sh", "/home/apps/start.sh"]