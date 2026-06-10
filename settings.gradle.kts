/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
  includeBuild("build-logic/common")
}

plugins {
  id("org.sonarsource.cloud-native.common-settings")
}

// TODO SONARPHP-1848: remove mavenLocal() once sonar-analyzer-commons PR #398 is released
dependencyResolutionManagement {
  repositories {
    mavenLocal()
  }
}

rootProject.name = "php"
includeBuild("build-logic")

include(":sonar-php-plugin")
include(":php-frontend")
include(":php-checks")
include(":php-custom-rules-plugin")
include(":its:plugin")
include(":its:ruling")
