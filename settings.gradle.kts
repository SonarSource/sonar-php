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
pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
      name = "artifactory"
      url = uri("https://repox.jfrog.io/repox/sonarsource")
      val artifactoryUsername =
        providers.environmentVariable("ARTIFACTORY_PRIVATE_USERNAME")
          .orElse(providers.gradleProperty("artifactoryUsername"))
      val artifactoryPassword =
        providers.environmentVariable("ARTIFACTORY_PRIVATE_PASSWORD")
          .orElse(providers.gradleProperty("artifactoryPassword"))

      if (artifactoryUsername.isPresent && artifactoryPassword.isPresent) {
        authentication {
          credentials {
            username = artifactoryUsername.get()
            password = artifactoryPassword.get()
          }
        }
      }
    }
  }
}

plugins {
  id("com.diffplug.blowdryerSetup") version "1.7.1"
}

rootProject.name = "php"
includeBuild("build-logic")

include(":sonar-php-plugin")
include(":php-frontend")
include(":php-checks")
include(":php-custom-rules-plugin")
include(":its:plugin")
include(":its:ruling")

gradle.allprojects {
  // this value is present on CI
  val buildNumber: String? = System.getProperty("buildNumber")
  project.extra["buildNumber"] = buildNumber
  val version = properties["version"] as String
  if (version.endsWith("-SNAPSHOT") && buildNumber != null) {
    val versionSuffix = if (version.count { it == '.' } == 1) ".0.$buildNumber" else ".$buildNumber"
    project.version =
      version.replace("-SNAPSHOT", versionSuffix).also {
        logger.lifecycle("Project ${project.name} version set to $it")
      }
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven {
      url = uri("https://repox.jfrog.io/repox/sonarsource")
      val artifactoryUsername =
        providers.environmentVariable("ARTIFACTORY_PRIVATE_USERNAME")
          .orElse(providers.gradleProperty("artifactoryUsername"))
      val artifactoryPassword =
        providers.environmentVariable("ARTIFACTORY_PRIVATE_PASSWORD")
          .orElse(providers.gradleProperty("artifactoryPassword"))

      if (artifactoryUsername.isPresent && artifactoryPassword.isPresent) {
        authentication {
          credentials {
            username = artifactoryUsername.get()
            password = artifactoryPassword.get()
          }
        }
      }
    }
  }
}
