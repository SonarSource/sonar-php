
set SONAR_PLUGINS_HOME=d:\java\sonar-2.1.1\extensions\plugins
set SONAR_PHP_PLUGIN_VERSION=0.3-SNAPSHOT

call mvn package



rem copy php\target\sonar-php-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%
rem copy php-codesniffer\target\sonar-php-codesniffer-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%
rem copy php-depend\target\sonar-php-depend-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%
rem copy php-pmd\target\sonar-php-pmd-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%
rem copy php-unit\target\sonar-php-unit-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%

set SONAR_PLUGINS_HOME=d:\java\sonar-2.2\extensions\plugins
copy target\sonar-php-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%

