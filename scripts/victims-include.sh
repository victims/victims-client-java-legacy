VICTIMS_CLIENT_VERSION="1.0-SNAPSHOT"
VICTIMS_JAR="victims-client-${VICTIMS_CLIENT_VERSION}-standalone.jar"
VICTIMS_HOME=${VICTIMS_HOME:-$(pwd)}
VICTIMS_CLIENT="${VICTIMS_HOME}/${VICTIMS_JAR}"
VICTIMS_SERVICE_URI=${VICTIMS_SERVICE_URI:-"http://www.victi.ms"}
VICTIMS_SERVICE_ENTRY=${VICTIMS_SERVICE_ENTRY:-"service/"}
VICTIMS_ENCODING=${VICTIMS_ENCODING:-"UTF-8"}
VICTIMS_CACHE_PURGE=${VICTIMS_CACHE_PURGE:-"false"}
VICTIMS_ALGORITHMS=${VICTIMS_ALGORITHMS:-"MD5,SHA1,SHA512"}
VICTIMS_DB_DRIVER=${VICTIMS_DB_DRIVER:-"org.h2.Driver"}
VICTIMS_DB_URL=${VICTIMS_DB_URL:-"jdbc:h2:${VICTIMS_HOME}/victims;MVCC=true"}
VICTIMS_DB_USER=${VICTIMS_DB_USER:-"victims"}
VICTIMS_DB_PASS=${VICTIMS_DB_PASS:-"victims"}
VICTIMS_DB_PURGE=${VICTIMS_DB_PURGE:-"false"}
VICTIMS_CLI_VERBOSE=${VICTIMS_CLI_VERBOSE:-"false"}
VICTIMS_CLI_INTERACTIVE=${VICTIMS_CLI_INTERACTIVE:-"false"}

victims_run(){
    java \
        -Dvictims.cli.repl=${VICTIMS_CLI_INTERACTIVE}\
        -Dvictims.cli.verbose=${VICTIMS_CLI_VERBOSE}\
        -Dvictims.home=${VICTIMS_HOME}\
        -Dvictims.service.uri=${VICTIMS_SERVICE_URI}\
        -Dvictims.service.entry=${VICTIMS_SERVICE_ENTRY}\
        -Dvictims.encoding=${VICTIMS_ENCODING}\
        -Dvictims.cache.purge=${VICTIMS_CACHE_PURGE}\
        -Dvictims.algorithms=${VICTIMS_ALGORITHMS}\
        -Dvictims.db.driver=${VICTIMS_DB_DRIVER}\
        -Dvictims.db.url=${VICTIMS_DB_URL}\
        -Dvictims.db.user=${VICTIMS_DB_USER}\
        -Dvictims.db.pass=${VICTIMS_DB_PASS}\
        -Dvictims.db.purge=${VICTIMS_DB_PURGE}\
        -jar ${VICTIMS_CLIENT} "$@" 
}
