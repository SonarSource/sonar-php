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
include(":its:plugin:tests")
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
      url = uri("https://repox.jfrog.io/repox/sonarsource-private-releases")
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
//  TODO remove before merge
    maven {
      url = uri("https://repox.jfrog.io/repox/sonarsource-public-dev")
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
