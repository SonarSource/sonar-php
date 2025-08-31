/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
plugins {
  id("org.sonarsource.cloud-native.code-style-conventions")
  id("org.sonarsource.cloud-native.artifactory-configuration")
  id("org.sonarsource.cloud-native.rule-api")
  id("org.sonarqube") version "6.3.1.5724"
}

artifactoryConfiguration {
  buildName = providers.environmentVariable("CIRRUS_REPO_NAME").orElse("sonar-php")
  artifactsToPublish = "org.sonarsource.php:sonar-php-plugin:jar"
  artifactsToDownload = ""
  repoKeyEnv = "ARTIFACTORY_DEPLOY_REPO"
  usernameEnv = "ARTIFACTORY_DEPLOY_USERNAME"
  passwordEnv = "ARTIFACTORY_DEPLOY_PASSWORD"
}

ruleApi {
  languageToSonarpediaDirectory = mapOf(
    "Php" to "$rootDir"
  )
}

spotless {
  java {
    // no Java sources in the root project
    target("")
  }
}

sonar {
  properties {
    property("sonar.projectName", "SonarSource PHP Analyzer")
    property("sonar.projectKey", "org.sonarsource.php:php")
    property("sonar.exclusions", "**/build/**/*")
    property("sonar.links.ci", "https://cirrus-ci.com/github/SonarSource/sonar-php")
    property("sonar.links.scm", "https://github.com/SonarSource/sonar-php")
    property("sonar.links.issue", "https://jira.sonarsource.com/browse/SONARPHP")
    property("sonar.sca.exclusions", "its/sources/**")
  }
}
