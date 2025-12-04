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
  id("org.sonarsource.php.code-style-convention")
  id("org.sonarsource.php.java-conventions")
  id("org.sonarsource.php.integration-test")
}

description = "PHP :: Integration Tests :: Plugin"

dependencies {
  "integrationTestCompileOnly"(project(":sonar-php-plugin", configuration = "shadow"))
  "integrationTestImplementation"(libs.sonar.orchestrator.junit5)
  "integrationTestImplementation"(libs.sonar.plugin.api)
  "integrationTestImplementation"(libs.sonar.ws)
  "integrationTestImplementation"(libs.sonar.lint.core)
  "integrationTestImplementation"(libs.sonar.lint.rpc.java.client)
  "integrationTestImplementation"(libs.sonar.lint.rpc.impl)
  "integrationTestImplementation"(libs.junit.platform)
  "integrationTestImplementation"(libs.junit.jupiter)
  "integrationTestImplementation"(libs.assertj.core)
  "integrationTestImplementation"(libs.awaitility)
  "integrationTestCompileOnly"(libs.jsr305)
}

(tasks["integrationTest"] as Test).filter {
  setIncludePatterns("Tests")
}

// Mandatory for the orchestrator in the "Tests" class, since it requires the custom rules plugin JAR
tasks.named("integrationTest") {
  dependsOn(":php-custom-rules-plugin:shadowJar")
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
