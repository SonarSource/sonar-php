
set SONAR_PLUGINS_HOME=d:\java\sonar-2.3\extensions\plugins
set SONAR_PHP_PLUGIN_VERSION=0.3-SNAPSHOT

call mvn clean package -DskipTests=false -Denforcer.skip=true -Ppackage
copy target\sonar-php-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%
