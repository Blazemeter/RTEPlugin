#!/bin/bash
# runs the plugin with the given JMeter version (through taurus)
# environment variables: JMETER_VERSION, JVM_VERSION (optional, by default is 8)
set -e

JMETER_PATH=${project.basedir}/.jmeter/$JMETER_VERSION
DEFAULT_JVM_VERSION=8
JVM_VERSION=${JVM_VERSION:-$DEFAULT_JVM_VERSION}

ERROR=0
[ "$JVM_VERSION" != "$DEFAULT_JVM_VERSION" ] && export JAVA_HOME=/usr/lib/jvm/java-${JVM_VERSION}-openjdk-amd64
bzt testJMeter.yaml -o modules.jmeter.version=$JMETER_VERSION -o modules.jmeter.path=$JMETER_PATH || ERROR=$?
[ "$JVM_VERSION" != "$DEFAULT_JVM_VERSION" ] && export JAVA_HOME=/usr/lib/jvm/java-${DEFAULT_JVM_VERSION}-openjdk-amd64
exit $ERROR
