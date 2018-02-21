# jmeter-rte-plugin

This project implements a jmeter plugin to support RTE (remote terminal emulation) protocols by providing config elements and samplers.

## Building

### Pre-requisites

- [jdk 1.8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [maven 3.3+](https://maven.apache.org/)
- [tn5250j library](https://github.com/tn5250j/tn5250j/archive/0.7.5.zip) installed in local maven repository:
  - If not already installed, run build with [ant](http://ant.apache.org/) `ant -f tn5250j-build.xml`.

### Build

To build the plugin and run all tests just run `mvn clean verify`

## Usage

To use the plugin install it (from `target` folder) in `lib/ext` folder of a jmeter installation, copy the `tn5250j.jar` to the same jmeter folder, run jmeter and check the new config and sampler elements available.
