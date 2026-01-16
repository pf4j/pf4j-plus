#!/bin/sh

#
# This script creates and run the pf4j-plus demo.
#

# create artifacts using maven
mvn clean package -DskipTests

# create demo-dist folder
rm -fr demo-dist
mkdir -p demo-dist/plugins

# copy artifacts to demo-dist folder
cp demo/app/target/pf4j-plus-demo-app-*.zip demo-dist/
cp demo/plugins/greeting-plugin/target/pf4j-plus-demo-greeting-plugin-*-all.jar demo-dist/plugins/
cp demo/plugins/welcome-plugin/target/pf4j-plus-demo-welcome-plugin-*-all.jar demo-dist/plugins/

cd demo-dist

# unzip app
jar xf pf4j-plus-demo-app-*.zip
rm pf4j-plus-demo-app-*.zip

# run demo
mv pf4j-plus-demo-app-*-SNAPSHOT.jar pf4j-plus-demo.jar
java -jar pf4j-plus-demo.jar

cd -
