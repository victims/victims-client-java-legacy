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

    victims> help
    help - displays help for each command
        help [<command>]

    sync - update the victims database definitions

    config - list, sets or gets configuration options for the victims client
        config list 
        config get victims.home 
        config set victims.home /home/user/example 

    scan - scans the supplied .jar file and reports any vulnerabilities
        scan path/to/file.jar

    last-update - returns the last time the database was updated

    exit - quit this program


Checking the last time you synchronized against the victims database
    
    victims> last-update
    Thu Jan 01 00:00:00 EST 1970


Synchronizing with the database

    victims> sync
    ok


Listing configuration settings
    (Default values will be used if they are not set)

    victims> config list
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
    
    victims> config set victims.home /tmp/.victims
    changed victims.home from (not set) to /tmp/.victims


Scanning a .jar file 

    victims> scan /home/gm/.m2/repository/org/springframework/spring/2.5.6/spring-2.5.6.jar
    /home/gm/.m2/repository/org/springframework/spring/2.5.6/spring-2.5.6.jar VULNERABLE! CVE-2009-1190 CVE-2011-2730 CVE-2010-1622 

Scripting victims tasks

    java -jar victims-client-1.0-SNAPSHOT-standalone.jar  <<EOF;
    last-update
    config set victims.cache.purge true
    config set victims.db.purge true
    config set victims.service.uri http://stage-victims.rhcloud.com
    sync
    scan /usr/share/java
    EOF

### Wrapper scripts

TODO - The plan is to build a CLI on top of the REPL capabilities using 
simple bash scripts. This is still something that I'm working on. However you 
can still use this as a CLI by specifying arguments e.g. 

    java -jar victims-client-1.0-SNAPSHOT-standalone.jar sync
    find . -name \*.jar -exec java -jar victims-client-1.0-SNAPSHOT-standalone.jar scan {} \;
    

