victims-client-java [![Build Status](https://travis-ci.org/victims/victims-client-java.png)](https://travis-ci.org/victims/victims-client-java)
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

Getting verbose output 
    $ java -Dvictims.cli.verbose=true -jar victims-client-1.0-SNAPSHOT-standalone.jar

Starting the REPL

    $ java -Dvictims.cli.repl=true -jar victims-client-1.0-SNAPSHOT-standalone.jar 


Asking for help
    > help
    help - displays help for each command
        help 
        help config

    scan-pom - Scan dependencies in a pom.xml file
        scan-pom pom.xml

    sync - update the victims database definitions
        sync 

    map - asynchronously maps a command to each input argument
        map scan file1.jar file2.jar file3.jar file4.jar

    config - list, sets or gets configuration options for the victims client
        config list
        config get victims.home
        config set victims.home /home/user/example

    scan - scans the supplied .jar file and reports any vulnerabilities
        scan path/to/file.jar
        scan /directory/full/of/jars

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


Scanning a .pom file
    
    > scan-pom test/pom.xml 
    org.jboss.resteasy, resteasy-fastinfoset-provider, 1.2.1.GA_CP01 VULNERABLE! CVE-2012-0818 


Mapping another victims command to a range of input values asynchronously.

    > map scan file1.jar file2.jar file3.jar

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

#### Synchronizing with victims service
    $ scripts/victims-update.sh 

#### Scanning a single .jar file
    $ scripts/victims-scan.sh file1.jar

#### Recursively scanning a directory 
    $ scripts/victims-scan.sh ~/.m2/repository

#### Scanning multiple input arguments 
    $ scripts/victims-map-scan.sh file1.jar file2.jar file3.jar **/*.jar

#### Overwriting configuration settings. 

Currently the wrapper script sets each configuration item based on an 
environment variable, falling back to safe defaults. These settings are: 

* VICTIMS_SERVICE_URI - The default URL where vulnerability definitions will be fetched.
* VICTIMS_SERVICE_ENTRY - The entry point to the restful web service.
* VICTIMS_ENCODING - The encoding used within victims data.
* VICTIMS_HOME - Where the victims database, victims cache and the client .jar file live.
* VICTIMS_CACHE_PURGE - Dumps the cache before the scan.
* VICTIMS_ALGORITHMS - Selects the algorithm/s to use for hashing.
* VICTIMS_DB_URL - Set the JDBC url for victims database connection 
* VICTIMS_DB_DRIVER - Set the JDBC database driver to use for the connection
* VICTIMS_DB_USER - Set the victims database user id
* VICTIMS_DB_PASS - Set the victims database password
* VICTIMS_DB_PURGE - Dumps the database of vulnerability definitions. 
 
