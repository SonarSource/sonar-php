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
  id("org.sonarsource.php.code-style-convention")
  id("org.sonarsource.php.java-conventions")
  id("org.sonarsource.php.integration-test")
}

description = "PHP :: Integration Tests :: Ruling"

dependencies {
  "integrationTestImplementation"(project(":sonar-php-plugin", configuration = "shadow"))
  "integrationTestImplementation"(libs.junit.jupiter)
  "integrationTestImplementation"(libs.assertj.core)
  "integrationTestImplementation"(libs.sonar.analyzer.commons)
  "integrationTestImplementation"(libs.sonar.orchestrator.junit5)
}

sonar {
  isSkipProject = true
}

codeStyleConvention {
  spotless {
    format("javaMisc") {
      targetExclude("**/integrationTest/**")
    }
  }
}
