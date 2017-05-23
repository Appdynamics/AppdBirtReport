#!/bin/bash
# Uncomment and set to the home directory of a local Java 8+ install
#JAVA_HOME=/home/cloud-user/AppDynamics/Controller/jre8/

if [ -f ${JAVA_HOME}/bin/java ]
then
	JAVA=${JAVA_HOME}/bin/java
else
	JAVA="java"
fi

echo `date`
$JAVA -Djava.util.logging.config.file=logging.properties -jar AppdReportGen.jar $*