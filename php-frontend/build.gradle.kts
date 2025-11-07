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
  id("org.sonarsource.cloud-native.java-conventions")
  id("org.sonarsource.cloud-native.code-style-conventions")
  id("java-test-fixtures")
}

description = "SonarSource PHP Analyzer :: Frontend"

dependencies {
  api(libs.sslr.core)
  api(libs.sonar.regex.parsing)

  compileOnly(libs.sonar.plugin.api)
  implementation(libs.commons.lang)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.mockito.core)
  testImplementation(libs.assertj.core)
  testImplementation(libs.sonar.plugin.api.test.fixtures)
  testImplementation(libs.sonar.plugin.api.impl)
  testImplementation(libs.sslr.testing.harness)
  testImplementation(libs.sonar.testing.harness)
  testImplementation(libs.sonar.analyzer.test.commons)
  testRuntimeOnly(libs.junit.platform.launcher)

  testFixturesImplementation(libs.sonar.analyzer.test.commons)
  testFixturesImplementation(libs.sonar.plugin.api.impl)
}
