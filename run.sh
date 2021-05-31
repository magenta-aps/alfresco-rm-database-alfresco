#!/bin/bash

MAVEN_OPTS="-Xms256m -Xmx2G -javaagent:/home/fhp/src/hotswap-agent.jar"  mvn clean install alfresco:run -DskipTests=true
