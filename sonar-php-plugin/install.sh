#!/bin/sh
SONAR_PLUGINS_HOME=/opt/sonar-2.2/extensions/plugins/
SONAR_PHP_PLUGIN_VERSION=0.3-SNAPSHOT

mvn -DskipTests=true package

cp target/sonar-php-plugin-$SONAR_PHP_PLUGIN_VERSION.jar $SONAR_PLUGINS_HOME
