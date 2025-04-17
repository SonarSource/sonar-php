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
  id("java-library")
  id("jacoco")
  id("com.gradleup.shadow")
}

dependencies {
  compileOnly(libs.sonar.plugin.api)
  compileOnly(project(":php-frontend"))

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.sonar.plugin.api.impl)
  testImplementation(testFixtures(project(":php-frontend")))
  testRuntimeOnly(libs.junit.platform.launcher)
}

description = "PHP Custom Rules Example for SonarQube"

tasks.jar {
  manifest {
    // More details about the attributes here: https://docs.sonarsource.com/sonarqube/latest/extension-guide/developing-a-plugin/plugin-basics/
    attributes(
      mapOf(
        "Plugin-ChildFirstClassLoader" to "false",
        "Plugin-Class" to "org.sonar.samples.php.PHPCustomRulesPlugin",
        "Plugin-Description" to "PHP Custom Rules Example for SonarQube",
        "Plugin-Developers" to "SonarSource Team",
        "Plugin-Display-Version" to version,
        "Plugin-Homepage" to "https://sonarsource.atlassian.net/browse/SONARPHP",
        "Plugin-IssueTrackerUrl" to "https://sonarsource.atlassian.net/browse/SONARPHP",
        "Plugin-Key" to "custom",
        "Plugin-License" to "GNU LGPL 3",
        "Plugin-Name" to "PHP Custom Rules",
        "Plugin-Organization" to "SonarSource",
        "Plugin-OrganizationUrl" to "https://www.sonarsource.com",
        "Plugin-RequiredForLanguages" to "php",
        "Plugin-SourcesUrl" to "https://github.com/SonarSource/sonar-php",
        "Plugin-Version" to project.version,
        "Sonar-Version" to "9.13",
        "SonarLint-Supported" to "true",
        "Version" to project.version.toString(),
        "Jre-Min-Version" to java.sourceCompatibility.majorVersion
      )
    )
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

plugins.withType<JacocoPlugin> {
  tasks["test"].finalizedBy("jacocoTestReport")
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
  }
}

tasks.shadowJar {
  exclude("**/*.php")
}
