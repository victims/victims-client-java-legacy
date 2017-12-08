victims-client-java [![Build Status](https://travis-ci.org/victims/victims-client-java-legacy.svg?branch=master](https://travis-ci.org/victims/victims-client-java-legacy)
===================


Standalone victims client to handle java artifacts.

*WARNING:* This is currently under heavy development.
## Building from source
```sh
mvn clean package
```
## Usage

```
$ java -jar target/victims-client-1.0-SNAPSHOT-standalone.jar 
USAGE: victims-client [OPTIONS]...[ARGUMENTS]

The victims java client will scan any supplied 
pom.xml, jar files or directories supplied as command line 
arguments. By default the output will only report when a 
vulnerable component was detected, or if an error condition exists. 

EXAMPLES:
  Scanning a pom.xml file:
    victims-client  --verbose project/pom.xml

  Scanning a jar file:
    victims-client --update --verbose file.jar

  Scanning a directory of files:
    victims-client --verbose --update --recursive  project/pom.xml

  Printing a jar file fingerprint:
    victims-client --jar-info  file.jar

  Run in interactive mode:
    victims-client --repl

OPTIONS:

  --db-status             displays date the database was last
                          updated

  --help                  show this help message

  --jar-info              displays fingerprint information of the
                          supplied jar file

  --recursive             recurses directory structure when
                          scanning a directory

  --repl                  run an interactive victims shell

  --update                synchronize with the victims web service

  --verbose               show verbose output

  --vicitms-db-driver     the jdbc driver to use when connecting
                          to the victims database

  --victims-cache-purge   purges all entries in the victims cache

  --victims-db-pass       set the password to use to connect to
                          the victims database

  --victims-db-purge      purges all entries in the victims
                          database

  --victims-db-url        the jdbc url connection string to use
                          with the victims database

  --victims-db-user       set the user to connect to the victims
                          database

  --victims-home          set the directory where victims data
                          should be stored

  --victims-service-entry the uri path to use when connecting to
                          the victims service

  --victims-service-uri   the uri to use to synchronize with the
                          victims database


```

## Proxies

Because this client requires access to the web, if your network requires
a proxy you must remember to set it via the [standard JVM arguments](http://stackoverflow.com/questions/120797/how-do-i-set-the-proxy-to-be-used-by-the-jvm).  A sample configuration might be:

```
-Dhttp.proxyHost=proxy.company.com
-Dhttp.proxyPort=80
-Dhttp.proxyUser=bob
-Dhttp.proxyPassword=secret
```

These settings can be provided on the command line when running the stand-alone JAR; when running tests via maven, it is advisable to export these settings in $JAVA_OPTS which will be picked up by the JVM that maven invokes.


