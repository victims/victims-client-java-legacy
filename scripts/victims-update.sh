#!/bin/bash
#==============================================================================
# perform a victims scan of a file or directory
#==============================================================================

source $(dirname $0)/victims-include.sh

echo "Updating victims vulnerability definitions. Last update: $(victims_run last-update)"
victims_run sync

