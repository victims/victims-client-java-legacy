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
    find $1 -name \*.jar | awk '{ print "scan " $1 }' | VICTIMS_CLI_INTERACTIVE=pipe victims_run
else
    victims_run scan $1
fi

