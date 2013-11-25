#!/bin/bash
#==============================================================================
# perform a victims scan of a file or directory
#==============================================================================

source $(dirname $0)/victims-include.sh

if [ $# == 0 ]; then 
    echo "usage: $0 <file|directory>"
    exit 1
fi

if [ -d $1 ]; then
    find $1 -name \*.jar | awk '{ print "scan " $1 }' | java -Dvictims.cli.repl=pipe -Dvictims.home=${VICTIMS_HOME} -jar ${VICTIMS_CLIENT}
else
    java -Dvictims.home=${VICTIMS_HOME} -jar ${VICTIMS_CLIENT} scan $1
fi

