@echo off
REM
REM This script creates and run the pf4j-plus demo.
REM

REM create artifacts using maven
call mvn clean package -DskipTests

REM create demo-dist folder
rmdir demo-dist /s /q 2>nul
mkdir demo-dist
mkdir demo-dist\plugins

REM copy artifacts to demo-dist folder
xcopy demo\app\target\pf4j-plus-demo-app-*.zip demo-dist /s /i /q
xcopy demo\plugins\greeting-plugin\target\pf4j-plus-demo-greeting-plugin-*-all.jar demo-dist\plugins /s /q
xcopy demo\plugins\welcome-plugin\target\pf4j-plus-demo-welcome-plugin-*-all.jar demo-dist\plugins /s /q

cd demo-dist

REM unzip app
jar xf pf4j-plus-demo-app-*.zip
del pf4j-plus-demo-app-*.zip

REM run demo
rename pf4j-plus-demo-app-*-SNAPSHOT.jar pf4j-plus-demo.jar
java -jar pf4j-plus-demo.jar

cd ..