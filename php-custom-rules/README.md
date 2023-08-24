[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=org.sonarsource.samples%3Aphp-custom-rules&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.sonarsource.samples%3Aphp-custom-rules)

This example demonstrates how to write **Custom Rules** for SonarPHP.

# API Changes

## 3.2 

* Added a new PHPCheck#terminate method, which is called at the end of the analysis 
* Added a new PhpFile#uri method to retrieve the underlying file URI
