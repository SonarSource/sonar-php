#!/bin/sh
SONAR_PLUGINS_HOME=/opt/sonar-2.0.1/extensions/plugins/
SONAR_PHP_PLUGIN_VERSION=0.2-SNAPSHOT

mvn -DskipTests=true package

cp php/target/sonar-php-plugin-$SONAR_PHP_PLUGIN_VERSION.jar $SONAR_PLUGINS_HOME
cp php-codesniffer/target/sonar-php-codesniffer-plugin-$SONAR_PHP_PLUGIN_VERSION.jar $SONAR_PLUGINS_HOME
cp php-depend/target/sonar-php-depend-plugin-$SONAR_PHP_PLUGIN_VERSION.jar $SONAR_PLUGINS_HOME
cp php-pmd/target/sonar-php-pmd-plugin-$SONAR_PHP_PLUGIN_VERSION.jar $SONAR_PLUGINS_HOME
cp php-unit/target/sonar-php-unit-plugin-$SONAR_PHP_PLUGIN_VERSION.jar $SONAR_PLUGINS_HOME


