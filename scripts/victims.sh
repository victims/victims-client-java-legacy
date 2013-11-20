#!/bin/bash
#
# This file is part of victims-client.
# Copyright (C) 2013 The Victims Project
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#  
# You should have received a copy of the GNU Affero General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
###############################################################################

# victims client configuration. 
# use environment variables to overwrite defaults.
VICTIMS_SERVICE_URI=${VICTIMS_SERVICE_URI:-"http://www.victi.ms"}
VICTIMS_SERVICE_ENTRY=${VICTIMS_SERVICE_ENTRY:-"service/"}
VICTIMS_ENCODING=${VICTIMS_ENCODING:-"UTF-8"}
VICTIMS_HOME=${VICTIMS_HOME:-"${HOME}/.victims"}
VICTIMS_CACHE_PURGE=${VICTIMS_CACHE_PURGE:-"false"}
VICTIMS_ALGORITHMS=${VICTIMS_ALGORITHMS:-"MD5,SHA1,SHA512"}
VICTIMS_DB_DRIVER=${VICTIMS_DB_DRIVER:-"org.h2.Driver"}
VICTIMS_DB_URL=${VICTIMS_DB_URL:-"jdbc:h2:${VICTIMS_HOME}/victims;MVCC=true"}
VICTIMS_DB_USER=${VICTIMS_DB_USER:-"victims"}
VICTIMS_DB_PASS=${VICTIMS_DB_PASS:-"victims"}
VICTIMS_DB_PURGE=${VICTIMS_DB_PURGE:-"false"}

# victims client version 
VICTIMS_CLIENT_VERSION="1.0-SNAPSHOT"

# standalone jar file
VICTIMS_JAR="victims-client-${VICTIMS_CLIENT_VERSION}-standalone.jar"

# client .jar file
VICTIMS_CLIENT="${VICTIMS_HOME}/${VICTIMS_JAR}"

# base command
VICTIMS_CMD="java -jar ${VICTIMS_CLIENT}"

debug_env(){
    echo "VICTIMS_CLIENT_VERSION = ${VICTIMS_CLIENT_VERSION}"
    echo "VICTIMS_JAR = ${VICTIMS_JAR}"
    echo "VICTIMS_CMD = ${VICTIMS_CMD}"
    echo "VICTIMS_SERVICE_URI = ${VICTIMS_SERVICE_URI}"
    echo "VICTIMS_SERVICE_ENTRY = ${VICTIMS_SERVICE_ENTRY}"
    echo "VICTIMS_ENCODING = ${VICTIMS_ENCODING}"
    echo "VICTIMS_HOME = ${VICTIMS_HOME}"
    echo "VICTIMS_CACHE_PURGE = ${VICTIMS_CACHE_PURGE}"
    echo "VICTIMS_ALGORITHMS = ${VICTIMS_ALGORITHMS}"
    echo "VICTIMS_DB_DRIVER = ${VICTIMS_DB_DRIVER}"
    echo "VICTIMS_DB_URL = ${VICTIMS_DB_URL}"
    echo "VICTIMS_DB_USER = ${VICTIMS_DB_USER}"
    echo "VICTIMS_DB_PASS = ${VICTIMS_DB_PASS}"
    echo "VICTIMS_DB_PURGE = ${VICTIMS_DB_PURGE}"
}

require_env(){
    if [ "${1}X" == "X" ]; then
        echo "error: $2"
        exit 1
    fi
}

require_path(){
    if [ ! -e "${1}" ]; then
        echo "error: required path '${1}' not found"
        exit 1
    fi
}

# ensure that everything is setup correctly 
checkenv(){
    require_env $(which java)     "Java is required to run victims client"
    require_env ${VICTIMS_HOME}   "VICTIMS_HOME environment variable not set"
    require_path "${VICTIMS_CLIENT}" 
}


last_updated(){

    last_update="$(runner 'last-update' | sed -e 's/>//')"
    echo "Vulnerability definitions last updated: ${last_update}."
}

runner(){

    ${VICTIMS_CMD} <<EOF
quiet
config set victims.service.uri "${VICTIMS_SERVICE_URI}"
config set victims.service.entry "${VICTIMS_SERVICE_ENTRY}"
config set victims.encoding "${VICTIMS_ENCODING}"
config set victims.home "${VICTIMS_HOME}"
config set victims.cache.purge "${VICTIMS_CACHE_PURGE}"
config set victims.algorithms "${VICTIMS_ALGORITHMS}"
config set victims.db.driver "${VICTIMS_DB_DRIVER}"
config set victims.db.url "${VICTIMS_DB_URL}"
config set victims.db.user "${VICTIMS_DB_USER}"
config set victims.db.pass "${VICTIMS_DB_PASS}"


config set victims.db.purge "${VICTIMS_DB_PURGE}"
$@
EOF
    if [ $? != 0 ]; then
        "error: victims command failed - $@"
    fi
}

usage(){
    cat <<USAGE

victims - Check jar files against a database of known vulnerable 
          artifacts. 

Usage:

    -h          Shows this help message.

    -u          Updates the victims vulnerability definitions.

    -s  <path>  Scans the supplied file or directory to see if 
                it matches a definition in the victims vulnerability
                database.

    -p          Scan dependencies listed in a pom.xml file.

    -i          Interactive mode.

    -f          Run a victims script


Examples:

  To update the victims vulnerability definitions - 
    victims -u 

  To scan a jar file for vulnerabilities: 
    victims -s example.jar 


For more detailed configuration options please refer to the victims man page. 

USAGE

}

main(){

    if [ -n "${VICTIMS_DEBUG+x}" ]; then 
        debug_env
    fi

    if [ $# -eq 0 ]; then
        usage
        exit 1
    fi

    checkenv
    last_updated

    while getopts ":hus:if:" opt; do
        case $opt in
            h)
                usage
                exit 1
                ;;
            \?)
                echo "Invalid option: -$OPTARG"
                usage
                exit 1
                ;;
            :)
                echo "Option -$OPTARG requires an argument"
                usage
                exit 1
                ;;
            u)
                echo "Synchronizing with the database. Please wait.."
                runner "sync" 
                ;;

            i)  echo "Entering interactive mode" 
                ${VICTIMS_CMD}
                ;;

            p)  echo "Scanning pom file $OPTARG"
                runner "scan-pom" "$OPTARG"
                ;;

            f)  echo "Running victims script"
                ${VICTIMS_CMD} < ${OPTARG}
                ;;

            s)
                echo "Scanning ${OPTARG}.."
                runner "scan" "$OPTARG" 
                ;;
        esac
    done
    exit $?
}

main "$@"

