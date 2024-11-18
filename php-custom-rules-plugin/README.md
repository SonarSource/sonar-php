# This example demonstrates how to write **Custom Rules** for SonarPHP.

## Build

### Gradle

The default build system is Gradle. To build the project and run its unit tests, execute this command from the project's root directory:

```shell
./gradlew build
```

### Maven

To use Maven instead of Gradle, replace the `build.gradle.kts` file with the `maven/pom.xml` file.

This change will also require to build the plugin dependency manually by executing this command from the project's root directory:

```shell
./gradlew publishToMavenLocal
```

## API Changes

### 3.39

* The classes `ExpectedIssuesParser`, `PHPCheckTest`, `PHPCheckVerifier` and `PhpTestFile` are moved to php-frontend testFixtures and the package `org.sonar.php.utils`

### 3.2 

* Added a new `PHPCheck#terminate` method, which is called at the end of the analysis 
* Added a new `PhpFile#uri` method to retrieve the underlying file URI
