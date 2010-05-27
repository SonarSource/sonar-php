
set SONAR_PLUGINS_HOME=d:\java\sonar-2.1.1\extensions\plugins
set SONAR_PHP_PLUGIN_VERSION=0.2-SNAPSHOT

rem mvn -DskipTests=true package

copy php\target\sonar-php-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%
copy php-codesniffer\target\sonar-php-codesniffer-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%
copy php-depend\target\sonar-php-depend-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%
copy php-pmd\target\sonar-php-pmd-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%
copy php-unit\target\sonar-php-unit-plugin-%SONAR_PHP_PLUGIN_VERSION%.jar %SONAR_PLUGINS_HOME%


pause