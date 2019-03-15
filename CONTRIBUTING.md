# Contributing

We are using a custom [checkstyle](http://checkstyle.sourceforge.net/index.html) configuration file which is based on google's one, is advisable to use one of the [google style configuration files](https://github.com/google/styleguide) in IDEs to reduce the friction with checkstyle and automate styling.

##Desing 

Check our general design of classes [UML](docs/class-diagram.puml).

## Building

### Pre-requisites

- [jdk 1.8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [maven 3.3+](https://maven.apache.org/)

### Build

To build the plugin and run all tests just run `mvn clean verify`. Since test suite include user interface tests, and such tests use UI with actual mouse interactions, avoid moving the mouse while running them.
Another option to avoid mouse interactions to affect tests is to build docker image and use it to run the build: `docker build -t jmeter-plugins-build jmeter-plugins-build && docker run --rm -v $(pwd):/src -v ~/.m2:/root/.m2 jmeter-plugins-build bash -c 'cd /src && /execute-on-vnc.sh mvn --batch-mode -Dmaven.repo.local=/root/.m2/repository clean verify'` for unix systems (some changes on the command would be required for Windows OS). 

We are using a Docker image with Maven and BZ Taurus to build and test the plugin. [Dockerfile](jmeter-plugins-build/Dockerfile).
If it is needed to modify the image, it should be tagged with a new version.

The following Docker commands should be ran to build and publish the new image to the private Docker registry.
  ```
  docker build -t 836525813842.dkr.ecr.us-east-1.amazonaws.com/jmeter-plugins-build:latest -t 836525813842.dkr.ecr.us-east-1.amazonaws.com/jmeter-plugins-build:$VERSION jmeter-plugins-build
  docker push 836525813842.dkr.ecr.us-east-1.amazonaws.com/jmeter-plugins-build
  ```

### Installation

To use the plugin, install it (by copying the jar from `target` folder and `xtn5250` and `dm3270-lib` dependencies from `.m2/repository` folder) in `lib/ext/` folder of the JMeter installation.

Run JMeter and check the new config and sampler elements available.