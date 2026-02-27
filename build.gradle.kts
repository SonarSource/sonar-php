/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
  id("org.sonarqube") version "7.2.2.6593"
}

artifactoryConfiguration {
  buildName = providers.environmentVariable("PROJECT").orElse("sonar-php")
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

subprojects {
  configurations.all {
    resolutionStrategy {
      // Pinned to avoid dependency risks
      force(libs.logback.classic)
    }
  }
}

sonar {
  properties {
    property("sonar.projectName", "SonarPHP")
    property("sonar.projectKey", "org.sonarsource.php:php")
    property("sonar.organization", "sonarsource")
    property("sonar.exclusions", "**/build/**/*")
    property("sonar.links.ci", "https://github.com/SonarSource/sonar-php/actions")
    property("sonar.links.scm", "https://github.com/SonarSource/sonar-php")
    property("sonar.links.issue", "https://jira.sonarsource.com/browse/SONARPHP")
    property("sonar.sca.exclusions", "its/sources/**")
  }
}
