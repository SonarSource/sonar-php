
set SONAR_PLUGINS_HOME=d:\java\sonar-2.5\extensions\plugins
set SONAR_PHP_PLUGIN_VERSION=0.4-SNAPSHOT

call mvn clean package -DskipTests=false
copy target\sonar-php-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%

pause