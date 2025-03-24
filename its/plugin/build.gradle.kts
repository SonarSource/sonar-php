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
  id("org.sonarsource.cloud-native.java-conventions")
  id("org.sonarsource.cloud-native.integration-test")
}

description = "PHP :: Integration Tests :: Plugin"

dependencies {
  // Mandatory for the orchestrator in the "Tests" class, since it requires the custom rules plugin JAR
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

integrationTest {
  testSources.set(file("projects"))
}

tasks.named<Test>("integrationTest") {
  dependsOn(":php-custom-rules-plugin:shadowJar")
  filter {
    setIncludePatterns("Tests")
  }
}

sonar.isSkipProject = true

codeStyleConvention {
  spotless {
    format("javaMisc") {
      targetExclude("**/integrationTest/**")
    }
  }
}
