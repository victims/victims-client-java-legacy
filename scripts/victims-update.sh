#!/bin/bash
#==============================================================================
# perform a victims scan of a file or directory
#==============================================================================

VICTIMS_CLIENT_VERSION="1.0-SNAPSHOT"
VICTIMS_JAR="victims-client-${VICTIMS_CLIENT_VERSION}-standalone.jar"
VICTIMS_HOME=${VICTIMS_HOME:-$(pwd)}
VICTIMS_CLIENT="${VICTIMS_HOME}/${VICTIMS_JAR}"

java -jar ${VICTIMS_CLIENT} sync
