/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

// Inspiration: https://docs.gradle.org/current/samples/sample_jvm_multi_project_with_additional_test_types.html

plugins {
  java
  id("org.sonarsource.php.java-conventions")
}

val integrationTest by sourceSets.creating

configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

tasks.register<Test>("integrationTest") {
  description = "Runs integration tests."
  group = "verification"
  inputs.dir("$rootDir/its/sources")
  inputs.property("SQ version", System.getProperty("sonar.runtimeVersion", "LATEST_RELEASE"))
  inputs.property("keep SQ running", System.getProperty("keepSonarqubeRunning", "false"))
  useJUnitPlatform()

  testClassesDirs = integrationTest.output.classesDirs
  classpath = configurations[integrationTest.runtimeClasspathConfigurationName] + integrationTest.output

  if (System.getProperty("sonar.runtimeVersion") != null) {
    systemProperty("sonar.runtimeVersion", System.getProperty("sonar.runtimeVersion", "LATEST_RELEASE"))
  }

  if (System.getProperty("keepSonarqubeRunning") != null) {
    systemProperty("keepSonarqubeRunning", System.getProperty("keepSonarqubeRunning"))
  }

  testLogging {
    exceptionFormat = TestExceptionFormat.FULL // log the full stack trace (default is the 1st line of the stack trace)
    events("skipped", "failed") // verbose log for failed and skipped tests (by default the name of the tests are not logged)
  }
}
