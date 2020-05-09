#!/bin/bash

JAR=canary-provider-1.3.2.jar
if [ ! -e $JAR ]; then
    JAR=target/$JAR
    if [ -e application.yaml ]; then
        cp application.yaml ./target/
    fi
fi
java $CMDVAR -jar ./$JAR