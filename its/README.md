# Run integration tests

# Prerequisites

* SonarQube ITs need Java 17 to run

# First time configuration

1. Make sure your Developer Box is properly setup (see xtranet)
2. Configure Orchestrator settings as described [here](https://github.com/SonarSource/orchestrator#configuration). The Artifactory API key and GitHub token are the only mandatory options. The GH token is a Personal Access Token (classic) with the `repo` scope permission, and SSO properly configured
3. Run `mvn clean install` a first time from this `its` folder so that test resources are built (like custom plugins)

# Running ITs from IntelliJ

1. From the root folder of the repository, first build sonar-php with `mvn clean install`. Without that the old JAR version from sonar-php-plugin/target will be used
2. Open a test class and run it

# Running ITs from command line

1. From the root folder of the repository, first build sonar-php with `mvn clean install`
2. Run `mvn verify -f its/pom.xml -Dsonar.runtimeVersion=<SQ server version>`

# Debug scanner in Integration tests

To debug the Scanner in ITs set the `SONAR_SCANNER_DEBUG_OPTS` like this:

```java
SonarScanner scanner = SonarScanner.create("projectDir")))
    .setEnvironmentVariable("SONAR_SCANNER_DEBUG_OPTS", "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=<port>");
 
ORCHESTRATOR.executeBuild(scanner);
```

The scanner will wait for a debug remote session to start.
