# This example demonstrates how to write **Custom Rules** for SonarPHP.

## API Changes

### 3.39

* The classes `ExpectedIssuesParser`, `PHPCheckTest`, `PHPCheckVerifier` and `PhpTestFile` are moved to php-frontend testFixtures and the package `org.sonar.php.utils`

### 3.2 

* Added a new `PHPCheck#terminate` method, which is called at the end of the analysis 
* Added a new `PhpFile#uri` method to retrieve the underlying file URI
