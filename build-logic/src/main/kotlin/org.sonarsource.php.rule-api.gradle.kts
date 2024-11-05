val ruleApiVersion = "2.9.0.4061"

repositories {
  maven {
    url = project.uri("https://repox.jfrog.io/repox/sonarsource-private-releases")
    authentication {
      credentials {
        val artifactoryUsername: String? by project
        val artifactoryPassword: String? by project
        username = artifactoryUsername
        password = artifactoryPassword
      }
    }
  }
  mavenCentral()
}

val ruleApi: Configuration = configurations.create("ruleApi")

dependencies {
  ruleApi("com.sonarsource.rule-api:rule-api:$ruleApiVersion")
}

tasks.register<JavaExec>("ruleApiUpdate") {
  description = "Update PHP rules description"
  group = "Rule API"
  workingDir = file("$projectDir")
  classpath = configurations.getByName("ruleApi")

  args("com.sonarsource.ruleapi.Main", "update")
}

val rule = providers.gradleProperty("rule")
val branch = providers.gradleProperty("branch")

tasks.register<JavaExec>("ruleApiGenerateRule") {
  description = "Generate PHP rule description"
  group = "Rule API"
  workingDir = file("$projectDir")
  classpath = configurations.getByName("ruleApi")

  args(
    buildList {
      add("com.sonarsource.ruleapi.Main")
      add("generate")
      add("-rule")
      add(rule.getOrElse(""))
      if (branch.isPresent) {
        add("-branch")
        add(branch.get())
      }
    },
  )
}
