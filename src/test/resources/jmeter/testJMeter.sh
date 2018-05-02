set -e

JMETER_VERSION=$1
JMETER_PATH=${project.basedir}/.jmeter/$JMETER_VERSION

mkdir -p $JMETER_PATH/lib/ext/ && cp -f lib/*.jar $JMETER_PATH/lib/ext/
bzt -o modules.jmeter.path=$JMETER_PATH -o modules.jmeter.version=$JMETER_VERSION testJMeter.yaml