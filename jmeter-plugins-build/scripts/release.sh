#/bin/bash
set -ex

(git branch -D production || true) && git checkout production && git merge master
# we need this to later on use with nextSnapshot due to https://github.com/mojohaus/versions-maven-plugin/issues/207
ORIGINAL_VERSION=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive exec:exec)
mvn versions:set -DremoveSnapshot
/execute-on-vnc.sh mvn --batch-mode clean verify
mvn --batch-mode scm:checkin -Dmessage="[RELEASE] Fix release version \${project.version}" -Dincludes=pom.xml
mvn --batch-mode scm:check-local-modification -DpushChanges=false || git status
mvn --batch-mode scm:tag -Dtag="\${project.version}"
git checkout master && git merge production
# we need this due to https://github.com/mojohaus/versions-maven-plugin/issues/207
mvn --batch-mode versions:set -DnextSnapshot=true -DnewVersion=$ORIGINAL_VERSION
mvn --batch-mode versions:set -DnextSnapshot=true
mvn --batch-mode scm:checkin -Dmessage="[RELEASE] Increase version to next development version \${project.version}" -Dincludes=pom.xml
