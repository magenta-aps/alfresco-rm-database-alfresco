#!/bin/bash

MAVEN_OPTS="-Xms256m -Xmx2G -javaagent:/home/flemming/src/hotswap-agent.jar -XXaltjvm=dcevm" mvn clean install alfresco:run -DskipTests=true
