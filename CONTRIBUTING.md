# Contributing

We are using a custom [checkstyle](http://checkstyle.sourceforge.net/index.html) configuration file which is based on google's one, is advisable to use one of the [google style configuration files](https://github.com/google/styleguide) in IDEs to reduce the friction with checkstyle and automate styling.

## Building

### Pre-requisites

- [jdk 1.8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [maven 3.3+](https://maven.apache.org/)
- [xtn5250 emulator](https://sourceforge.net/projects/xtn5250/) installed in local maven repository:
  - If not already installed, run 
    ```
    mvn com.googlecode.maven-download-plugin:download-maven-plugin:1.4.0:wget -Ddownload.url=https://sourceforge.net/projects/xtn5250/files/xtn5250/1.19m/xtn5250_119m.jar
    mvn install:install-file -Dfile=target/xtn5250_119m.jar -DgroupId=net.sourceforge.xtn5250 -DartifactId=xtn5250 -Dversion=1.19m -Dpackaging=jar
    ```
- [dm3270 emulator](http://dmolony.github.io/) installed in local maven repository:
  - If not already installed, run 
    ```
    mvn com.googlecode.maven-download-plugin:download-maven-plugin:1.4.0:wget -Ddownload.url=https://github.com/dmolony/dm3270/releases/download/v0.5-beta-37/dm3270.jar
    mvn install:install-file -Dfile=target/dm3270.jar -DgroupId=com.bytezone.dm3270 -DartifactId=dm3270 -Dversion=0.5-beta-37 -Dpackaging=jar
    ```

### Build

To build the plugin and run all tests just run `mvn clean verify`

We are using a Docker image with Maven and BZ Taurus to build and test the plugin. [Dockerfile](jmeter-plugins-build/Dockerfile).
If it is needed to modify the image, it should be tagged with a new version.

The following Docker commands should be ran to build and publish the new image to the private Docker registry.
  ```
  docker build -t 836525813842.dkr.ecr.us-east-1.amazonaws.com/jmeter-plugins-build:latest -t 836525813842.dkr.ecr.us-east-1.amazonaws.com/jmeter-plugins-build:$VERSION jmeter-plugins-build
  docker push 836525813842.dkr.ecr.us-east-1.amazonaws.com/jmeter-plugins-build
  ```

### Installation

To use the plugin, install it (by copying the jar from `target` folder) in `lib/ext/` folder of the JMeter installation. Also copy `xtn5250_119m.jar` and `dm3270.jar` to the same JMeter folder.

Run JMeter and check the new config and sampler elements available.