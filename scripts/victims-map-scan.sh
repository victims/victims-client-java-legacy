#!/bin/bash
#==============================================================================
# perform a victims scan of a bunch of input files (use with xargs)
#==============================================================================

VICTIMS_CLIENT_VERSION="1.0-SNAPSHOT"
VICTIMS_JAR="victims-client-${VICTIMS_CLIENT_VERSION}-standalone.jar"
VICTIMS_HOME=${VICTIMS_HOME:-$(pwd)}
VICTIMS_CLIENT="${VICTIMS_HOME}/${VICTIMS_JAR}"

if [ $# == 0 ]; then 
    echo "usage: $0 <file> ..[files]"
    exit 1
fi

java -jar ${VICTIMS_CLIENT} map scan "$@"

