victims-client-java
===================

Standalone victims client to handle java artifacts.

*WARNING:* This is currently under heavy development.
## Building from source
```sh
mvn clean package
```
## Usage

The CLI is essentially a collection of wrapper scripts that 
execute on top of the victims client REPL. 

### Using the repl directly

Starting the REPL

    $ java -jar victims-client-1.0-SNAPSHOT-standalone.jar 

Asking for help

    help - displays help for each command
        help 
        help config

    sync - update the victims database definitions
        sync 

    config - list, sets or gets configuration options for the victims client
        config list
        config get victims.home
        config set victims.home /home/user/example

    scan - scans the supplied .jar file and reports any vulnerabilities
        scan path/to/file.jar
        scan /directory/full/of/jars

    quiet - produces less verbose output
        quiet 

    last-update - returns the last time the database was updated
        last-update 

    exit - exit from interactive mode


Checking the last time you synchronized against the victims database
    
    > last-update
    Thu Jan 01 00:00:00 EST 1970


Synchronizing with the database

    > sync
    ok


Listing configuration settings
    (Default values will be used if they are not set)

    > config list
    victims.service.uri = null
    victims.service.entry = null
    victims.encoding = null
    victims.home = null
    victims.cache.purge = null
    victims.algorithms = null
    victims.db.driver = null
    victims.db.url = null
    victims.db.user = null
    victims.db.pass = null
    victims.db.purge = null


Setting configuration values
    
    > config set victims.home /tmp/.victims


Scanning a .jar file 

    > scan /home/gm/.m2/repository/org/springframework/spring/2.5.6/spring-2.5.6.jar
    /home/gm/.m2/repository/org/springframework/spring/2.5.6/spring-2.5.6.jar VULNERABLE! CVE-2009-1190 CVE-2011-2730 CVE-2010-1622 

Scripting victims tasks

    java -jar victims-client-1.0-SNAPSHOT-standalone.jar  <<EOF
    last-update
    config set victims.cache.purge true
    config set victims.db.purge true
    config set victims.service.uri http://stage-victims.rhcloud.com
    sync
    scan /usr/share/java
    EOF

### Wrapper scripts

#### Execution of single command 

    java -jar victims-client-1.0-SNAPSHOT-standalone.jar sync

#### Scanning multiple files

    find . -name \*.jar -exec java -jar victims-client-1.0-SNAPSHOT-standalone.jar scan {} \;
 

#### Using the victims wrapper script

An example shell script that wraps the victims .jar file is located in 
src/resources/victims.  
    
```
$ src/resources/victims
Vulnerability definitions last updated: Tue Nov 19 15:33:33 EST 2013.

victims - Check jar files against a database of known vulnerable 
          artifacts. 

Usage:

    -h          Shows this help message.

    -u          Updates the victims vulnerability definitions.

    -s  <path>  Scans the supplied file or directory to see if 
                it matches a definition in the victims vulnerability
                database.

    -i          Interactive mode.

Examples:

  To update the victims vulnerability definitions - 
    victims -u 

  To scan a jar file for vulnerabilities: 
    victims -s example.jar 
```

#### Overwriting configuration settings. 

Currently the wrapper script sets each configuration item based on an 
environment variable, falling back to safe defaults. These settings are: 

* VICTIMS_SERVICE_URI - The default URL where vulnerability definitions will be fetched.
* VICTIMS_SERVICE_ENTRY - The entry point to the restful web service.
* VICTIMS_ENCODING - The encoding used within victims data.
* VICTIMS_HOME - Where the victims database, victims cache and the client .jar file live.
* VICTIMS_CACHE_PURGE - Dumps the cache before the scan.
* VICTIMS_ALGORITHMS - Selects the algorithm/s to use for hashing.
* VICTIMS_DB_PURGE - Dumps the database of vulnerability definitions. 
 
