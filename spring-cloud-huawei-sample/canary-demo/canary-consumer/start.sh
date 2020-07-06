#!/bin/bash

JAR=canary-consumer-1.3.3-Hoxton-SNAPSHOT.jar
if [ ! -e $JAR ]; then
    JAR=target/$JAR
    if [ -e application.yaml ]; then
        cp application.yaml ./target/
    fi
fi
java $CMDVAR -jar ./$JAR