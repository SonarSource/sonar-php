# This example demonstrates how to write **Custom Rules** for SonarPHP.

## Build

### Gradle

The default build system is Gradle. To build the project and run its unit tests, execute this command from the project's root directory:

```shell
./gradlew build
```

### Maven

The custom rules example can also be built with Maven. The Maven and Gradle build files can coexist in the same directory.

The Maven build requires `php-frontend` to be available in the local Maven repository. Build and install it manually by executing these commands from the project's root directory:

```shell
./gradlew :php-frontend:jar :php-frontend:testFixturesJar
mvn install:install-file \
  -Dfile=php-frontend/build/libs/php-frontend-4.0-SNAPSHOT.jar \
  -DgroupId=org.sonarsource.php \
  -DartifactId=php-frontend \
  -Dversion=4.0-SNAPSHOT \
  -Dpackaging=jar \
  -DgeneratePom=true
mvn install:install-file \
  -Dfile=php-frontend/build/libs/php-frontend-4.0-SNAPSHOT-test-fixtures.jar \
  -DgroupId=org.sonarsource.php \
  -DartifactId=php-frontend \
  -Dversion=4.0-SNAPSHOT \
  -Dpackaging=jar \
  -Dclassifier=test-fixtures \
  -DgeneratePom=false
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
