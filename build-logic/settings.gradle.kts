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
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
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
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
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
