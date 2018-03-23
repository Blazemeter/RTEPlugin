# Contributing

We are using a custom [checkstyle](http://checkstyle.sourceforge.net/index.html) configuration file which is based on google's one, is advisable to use one of the [google style configuration files](https://github.com/google/styleguide) in IDEs to reduce the friction with checkstyle and automate styling.

## Building

### Pre-requisites

- [jdk 1.8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [maven 3.3+](https://maven.apache.org/)
- [xtn5250 library](https://sourceforge.net/projects/xtn5250/) installed in installed in local maven repository:
  - If not already installed, run 
    ```
    mvn com.googlecode.maven-download-plugin:download-maven-plugin:1.4.0:wget -Ddownload.url=https://sourceforge.net/projects/xtn5250/files/xtn5250/1.19m/xtn5250_119m.jar
    mvn install:install-file -Dfile=target/xtn5250_119m.jar -DgroupId=net.sourceforge.xtn5250 -DartifactId=xtn5250 -Dversion=1.19m -Dpackaging=jar
    ```
### Build

To build the plugin and run all tests just run `mvn clean verify`

We are using a Docker image with Maven and BZ Taurus to build and test the plugin. The Dockerfile can be found on this repo. 

### Installation

To use the plugin, install it (by copying the jar from `target` folder) in `lib/ext/` folder of the JMeter installation. Also copy `xtn5250_119m.jar` to the same JMeter folder.

Run JMeter and check the new config and sampler elements available.