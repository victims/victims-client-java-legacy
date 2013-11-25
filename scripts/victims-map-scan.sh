#!/bin/bash
#==============================================================================
# perform a victims scan of a bunch of input files (use with xargs)
#==============================================================================

source $(dirname $0)/victims-include.sh

if [ $# == 0 ]; then 
    echo "usage: $0 <file> ..[files]"
    exit 1
fi

victims_run map scan "$@"
