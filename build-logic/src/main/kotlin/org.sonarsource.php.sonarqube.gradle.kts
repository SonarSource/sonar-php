plugins {
  id("org.sonarqube")
}

sonar {
  properties {
    property("sonar.projectName", "SonarSource PHP Analyzer")
    property("sonar.projectKey", "org.sonarsource.php:php")
    property("sonar.exclusions", "**/build/**/*")
    property("sonar.links.ci", "https://cirrus-ci.com/github/SonarSource/sonar-php")
    property("sonar.links.scm", "https://github.com/SonarSource/sonar-php")
    property("sonar.links.issue", "https://jira.sonarsource.com/browse/SONARPHP")
  }
}
