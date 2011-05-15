#!/bin/sh
SONAR_PLUGINS_HOME=/home/akram/sonar/sonar-2.7/extensions/plugins/
SONAR_PHP_PLUGIN_VERSION=0.5-SNAPSHOT

mvn -DskipTests=true package

cp target/sonar-php-plugin-$SONAR_PHP_PLUGIN_VERSION.jar $SONAR_PLUGINS_HOME
