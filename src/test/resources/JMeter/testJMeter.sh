set -e

JMETER_VERSION=$1

mkdir -p jmeter/$JMETER_VERSION/lib/ext/ && cp -f target/*.jar jmeter/$JMETER_VERSION/lib/ext/
cp -f target/taurus/*.jar jmeter/$JMETER_VERSION/lib/ext/
bzt -o modules.jmeter.path=jmeter/$JMETER_VERSION -o modules.jmeter.version=$JMETER_VERSION src/test/resources/JMeter/testJMeter.yaml
