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
  id("org.sonarsource.cloud-native.code-style-conventions")
  id("org.sonarsource.cloud-native.java-conventions")
  id("org.sonarsource.cloud-native.integration-test")
}

description = "PHP :: Integration Tests :: Ruling"

dependencies {
  "integrationTestImplementation"(project(":sonar-php-plugin", configuration = "shadow"))
  "integrationTestImplementation"(libs.junit.jupiter)
  "integrationTestImplementation"(libs.assertj.core)
  "integrationTestImplementation"(libs.sonar.analyzer.commons)
  "integrationTestImplementation"(libs.sonar.orchestrator.junit5)
  "integrationTestRuntimeOnly"(libs.junit.platform.launcher)
}

integrationTest {
  testSources = rootProject.file("its/sources")
}

sonar.isSkipProject = true

codeStyleConvention {
  spotless {
    format("javaMisc") {
      targetExclude("**/integrationTest/**")
    }
  }
}
