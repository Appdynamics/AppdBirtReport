#!/bin/bash
JAVA_HOME=/home/cloud-user/AppDynamics/Controller/jre8/

echo `date`
$JAVA_HOME/bin/java -jar /home/cloud-user/tmp/AppdReportGen.jar $*