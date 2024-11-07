import org.sonarsource.php.registerCleanupTask

plugins {
  id("org.sonarsource.php.java-conventions")
  id("org.sonarsource.php.code-style-convention")
}

dependencies {
  implementation(libs.sonar.plugin.api)
  implementation(project(":sonar-php-plugin"))
  implementation(project(":php-frontend"))
}

description = "PHP Custom Rules Example for SonarQube"

tasks.jar {
  manifest {
    attributes(
      mapOf(
        "Plugin-Class" to "org.sonar.samples.php.CustomPHPRulesPlugin",
        "Base-Plugin" to "php",
      ),
    )
  }
}

val cleanupTask = registerCleanupTask()
