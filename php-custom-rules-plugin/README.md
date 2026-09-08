# This example demonstrates how to write **Custom Rules** for SonarPHP.

## Build

### Gradle

The default build system is Gradle. To build the project and run its unit tests, execute this command from the project's root directory:

```shell
./gradlew build
```

### Maven

The custom rules example can also be built with Maven. The Maven and Gradle build files can coexist in the same directory.

The Maven build requires `php-frontend` to be available in the local Maven repository. Publish it locally by executing this command from the project's root directory:

```shell
./gradlew :php-frontend:publishToMavenLocal
```

Then execute this command from the `php-custom-rules-plugin` directory:

```shell
mvn package
```

## API Changes

### 3.39

* The classes `ExpectedIssuesParser`, `PHPCheckTest`, `PHPCheckVerifier` and `PhpTestFile` are moved to php-frontend testFixtures and the package `org.sonar.php.utils`

### 3.2 

* Added a new `PHPCheck#terminate` method, which is called at the end of the analysis 
* Added a new `PhpFile#uri` method to retrieve the underlying file URI
