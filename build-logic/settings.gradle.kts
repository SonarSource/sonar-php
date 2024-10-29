rootProject.name = "sonar-php"

dependencyResolutionManagement {
  repositories {
    maven {
      url = uri("https://repox.jfrog.io/repox/sonarsource")

      val artifactoryUsername =
        providers.environmentVariable("ARTIFACTORY_PRIVATE_USERNAME").orElse(
          providers.gradleProperty("artifactoryUsername")
        )
      val artifactoryPassword =
        providers.environmentVariable("ARTIFACTORY_PRIVATE_PASSWORD").orElse(
          providers.gradleProperty("artifactoryPassword")
        )

      if (artifactoryUsername.isPresent && artifactoryPassword.isPresent) {
        authentication {
          credentials {
            username = artifactoryUsername.get()
            password = artifactoryPassword.get()
          }
        }
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

pluginManagement {
  repositories {
    maven {
      url = uri("https://repox.jfrog.io/repox/sonarsource")

      val artifactoryUsername =
        providers.environmentVariable("ARTIFACTORY_PRIVATE_USERNAME").orElse(
          providers.gradleProperty("artifactoryUsername")
        )
      val artifactoryPassword =
        providers.environmentVariable("ARTIFACTORY_PRIVATE_PASSWORD").orElse(
          providers.gradleProperty("artifactoryPassword")
        )

      if (artifactoryUsername.isPresent && artifactoryPassword.isPresent) {
        authentication {
          credentials {
            username = artifactoryUsername.get()
            password = artifactoryPassword.get()
          }
        }
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}
