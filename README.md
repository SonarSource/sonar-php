# Code Quality and Security for PHP 

<p align="center">
  <img alt="Cirrus CI - Task and Script Build Status" src="https://img.shields.io/cirrus/github/SonarSource/sonar-php">
  <img alt="Quality Gate Status" src="https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=org.sonarsource.php%3Aphp&metric=alert_status">
  <img alt="Coverage" src="https://next.sonarqube.com/sonarqube/api/project_badges/measure?project=org.sonarsource.php%3Aphp&metric=coverage">
  <img alt="Maven Central" src="https://img.shields.io/maven-central/v/org.sonarsource.php/sonar-php-plugin">
  <img alt="GitHub" src="https://img.shields.io/github/license/SonarSource/sonar-php">
</p>

This SonarSource project is a [static code analyser](https://en.wikipedia.org/wiki/Static_program_analysis) for PHP language used as an extension for the [SonarQube](https://www.sonarqube.org/) platform. It will allow you to produce stable and easily supported code by helping you find and correct bugs, vulnerabilities and smells in your code.


# Features
* 200+ rules
* Supports up to PHP 8.3
* Metrics (complexity, number of lines etc.)
* Import of [unit test and coverage results](https://docs.sonarqube.org/latest/analysis/coverage/)
* Support of [custom rules](https://docs.sonarqube.org/latest/analysis/languages/php/)

# Useful links

* [Project homepage](https://www.sonarsource.com/php/)
* [Documentation](https://docs.sonarqube.org/latest/analysis/languages/php/)
* [Issue tracking](https://jira.sonarsource.com/browse/SONARPHP)
* [Available rules](https://rules.sonarsource.com/php)
* [SonarSource Community Forum](https://community.sonarsource.com/)
* [Demo project analysis](https://sonarcloud.io/dashboard?id=monica)

# More documentation

* [Control Flow Graph documentation](doc/CFG.md)

Have question or feedback?
--------------------------

To provide feedback (request a feature, report a bug etc.) use the [SonarSource Community Forum](https://community.sonarsource.com/). Please do not forget to specify the language (PHP!), plugin version and SonarQube version.

If you have a question on how to use plugin (and the [docs](https://docs.sonarqube.org/latest/analysis/languages/php/) don't help you), we also encourage you to use the community forum.

# Contributing

### Topic in SonarSource Community Forum

To request a new feature, please create a new thread in [SonarSource Community Forum](https://community.sonarsource.com/). Even if you plan to implement it yourself and submit it back to the community, please start a new thread first to be sure that we can follow up on it.

### Pull Request (PR)
To submit a contribution, create a pull request for this repository. Please make sure that you follow our [code style](https://github.com/SonarSource/sonar-developer-toolset) and all [tests](#testing) are passing.

### Custom Rules
If you have an idea for a rule but you are not sure that everyone needs it you can implement a [custom rule](https://docs.sonarqube.org/latest/analysis/languages/php/) available only for you.

#### Custom Rules API Changes
- **3.32** (October 2023)
    * Additional `newIssue` endpoint added to the `CheckContext` API interface
- **3.15** (January 2021)
    * `PHPCustomRulesDefinition` was removed, it was deprecated since version 2.13 (March 2018)
    * Removed dependency on sslr-squid-bridge which is not maintained anymore
- **3.11**, support of PHP 8:
    * `ParameterTree#type()` is deprecated. Use `ParameterTree#declaredType()` instead.
    * `ReturnTypeClauseTree#type()` is deprecated. Use `ReturnTypeClauseTree#declaredType()` instead.
    * `ClassPropertyDeclarationTree#typeAnnotation()` is deprecated. Use `ClassPropertyDeclarationTree#declaredType()` instead.
    * `CatchBlockTree#variable()` can now return `NULL`.
    * `FunctionCallTree#arguments()` is deprecated. Use `FunctionCallTree#callArguments()` instead.
    * `AnonymousClassTree#arguments()` is deprecated. Use `AnonymousClassTree#callArguments()` instead.
    * New tree: `CallArgumentTree`. This tree wraps expressions passed as arguments now.   
    * New kind of expression: `ThrowExpressionTree`.
    * New kind of expression: `MatchExpressionTree`.
    * `ParameterTree` now has a `visibility` method.
    

# <a name="testing"></a>Testing
To run tests locally follow these instructions.

### Build the Project and Run Unit Tests
To build the plugin and run its unit tests, execute this command from the project's root directory (you will need [Maven](https://maven.apache.org/)):
```shell
mvn clean install
```

### Integration Tests
To run integration tests, you will need to create a properties file like the one shown below, and set its location in an environment variable named `ORCHESTRATOR_CONFIG_URL`.
```properties
# version of SonarQube server
sonar.runtimeVersion=9.9
```
Before running any of integration tests make sure the submodules are checked out:
```shell
  git submodule init
  git submodule update
```
#### Plugin Test
The "Plugin Test" is an additional integration test which verifies plugin features such as metric calculation, coverage etc. To launch it, execute this command from directory `its/plugin`:
```shell
mvn clean install
```

#### Ruling Test
The "Ruling Test" is a special integration test which launches the analysis of a large code base, saves the issues created by the plugin in report files, and then compares those results to the set of expected issues (stored as JSON files). To launch ruling test:
```shell
cd its/ruling
mvn clean install
```

This test gives you the opportunity to examine the issues created by each rule and make sure they're what you expect. You can inspect new/lost issues checking web-pages mentioned in the logs at the end of analysis:
```
INFO  - HTML Issues Report generated: /path/to/project/sonar-php/its/sources/src/.sonar/issues-report/issues-report.html
INFO  - Light HTML Issues Report generated: /path/to/project/sonar-php/its/sources/src/.sonar/issues-report/issues-report-light.html
```
If everything looks good to you, you can copy the file with the actual issues located at
```
sonar-php/its/ruling/target/actual/
```
into the directory with the expected issues
```
sonar-php/its/ruling/src/test/resources/expected/
```

### License

Copyright 2010-2021 SonarSource.

Licensed under the [GNU Lesser General Public License, Version 3.0](https://www.gnu.org/licenses/lgpl.txt)
