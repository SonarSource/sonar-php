/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
  alias(libs.plugins.spotless)
  id("org.sonarsource.php.artifactory-configuration")
  id("org.sonarsource.php.rule-api")
  id("org.sonarsource.php.sonarqube")
}

val kotlinGradleDelimiter = "(package|import|plugins|pluginManagement|dependencyResolutionManagement|repositories) "
spotless {
  // Mainly used to define spotless configuration for the build-logic
  encoding(Charsets.UTF_8)
  kotlinGradle {
    ktlint().setEditorConfigPath("$rootDir/.editorconfig")
    target("*.gradle.kts", "build-logic/*.gradle.kts", "/build-logic/src/**/*.gradle.kts")
    licenseHeaderFile(
      rootProject.file("LICENSE_HEADER"),
      kotlinGradleDelimiter,
    ).updateYearWithLatest(true)
  }
  kotlin {
    ktlint().setEditorConfigPath("$rootDir/.editorconfig")
    target("/build-logic/src/**/*.kt")
    licenseHeaderFile(rootProject.file("LICENSE_HEADER")).updateYearWithLatest(true)
  }
}

tasks.artifactoryPublish { skip = true }
