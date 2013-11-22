#!/bin/bash
#==============================================================================
# perform a victims scan of a file or directory
#==============================================================================

VICTIMS_CLIENT_VERSION="1.0-SNAPSHOT"
VICTIMS_JAR="victims-client-${VICTIMS_CLIENT_VERSION}-standalone.jar"
VICTIMS_HOME=${VICTIMS_HOME:-$(pwd)}
VICTIMS_CLIENT="${VICTIMS_HOME}/${VICTIMS_JAR}"

if [ $# == 0 ]; then 
    echo "usage: $0 <file|directory>"
    exit 1
fi

if [ -d $1 ]; then
    find $1 -name \*.jar | awk '{ print "scan " $1 }' | java -jar ${VICTIMS_CLIENT}
else
    java -jar ${VICTIMS_CLIENT} scan $1
fi

