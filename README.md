# jmeter-rte-plugin

This project implements a jmeter plugin to support RTE (remote terminal emulation) protocols by providing config elements and samplers.

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

## Usage

To use the plugin install it (from `target` folder) in `lib/ext` folder of a jmeter installation, copy the `tn5250j.jar` to the same jmeter folder, run jmeter and check the new config and sampler elements available.

## Contributing

We are using a custom [checkstyle](http://checkstyle.sourceforge.net/index.html) configuration file which is based on google one, is advisable to use one of the [google style configuration files](https://github.com/google/styleguide) in IDEs to reduce the friction with checkstyle and automate styling.