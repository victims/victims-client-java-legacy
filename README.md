victims-client-java
===================
Standalone victims client to handle java artifacts.

*WARNING:* This is currently under heavy development.
## Building from source
```sh
mvn clean package
```
## Usage
Here are some sample usage of the generated standalone artifact.
### Scan a directory or file and check for vulnerabilities
```sh
[abn@whippersnapper target (master)]$ java -jar victims-client-1.0-SNAPSHOT-standalone.jar /home/abn/.m2/repository/org/springframework/spring/2.5.6/spring-2.5.6.jar
Synchronizing database with web service.
Sync complete.
Scanning: /home/abn/.m2/repository/org/springframework/spring/2.5.6/spring-2.5.6.jar
Spring Framework:null:2.5.6 matched [CVE-2009-1190, CVE-2010-1622, CVE-2011-2730]
Scanning Complete: /home/abn/.m2/repository/org/springframework/spring/2.5.6/spring-2.5.6.jar
```
_*Note:*_ This will store the synchronized database to _.victims-client-cache/db_ in the current working directory. Configuration is currently not supported.
### Scan a directory or file to get JSON string of the VictimsRecord
```sh
[abn@whippersnapper target (master)]$ java -cp victims-client-1.0-SNAPSHOT-standalone.jar com.redhat.victims.VictimsScannerCLI -h
usage: VictimsScannerCLI
 -h,--help           print this message
 -o,--output <arg>   output to this file, if not provided standard-out
                     will be used
```
### Get updates/removes from the victims server. (The default server is [here](https://victims-websec.rhcloud.com/))
```sh
[abn@whippersnapper target (master)]$ java -cp victims-client-1.0-SNAPSHOT-standalone.jar com.redhat.victims.VictimsServiceCLI -h
usage: VictimsServiceCLI
 -d,--date <arg>     date-time since which updates/removals are required
                     (format:yyyy-MM-dd'T'HH:mm:ss)
 -h,--help           print this message
 -o,--output <arg>   output to this file, if not provided standard-out
                     will be used
 -r,--removes        fetch removals from the server (incompatible with -u)
 -s,--server <arg>   use this as the victims server
 -u,--updates        fetch updates from the server (incompatible with -r)
```
