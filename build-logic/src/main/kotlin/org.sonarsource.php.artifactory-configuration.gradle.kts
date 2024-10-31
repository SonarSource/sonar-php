import org.sonarsource.php.ArtifactoryConfiguration
import org.sonarsource.php.signingCondition

plugins {
  id("com.jfrog.artifactory")
  signing
  `maven-publish`
}

val artifactoryConfiguration = extensions.create<ArtifactoryConfiguration>("artifactoryConfiguration")

publishing {
  publications.create<MavenPublication>("mavenJava") {
    pom {
      name.set("SonarSource PHP Analyzer")
      description.set(project.description)
      url.set("http://www.sonarqube.org/")
      organization {
        name.set("SonarSource")
        url.set("http://www.sonarsource.com/")
      }
      licenses {
        license {
          name.set("GNU LGPL 3")
          url.set("http://www.gnu.org/licenses/lgpl.txt")
          distribution.set("repo")
        }
      }
      scm {
        url.set("https://github.com/SonarSource/sonar-php")
      }
      developers {
        developer {
          id.set("sonarsource-team")
          name.set("SonarSource Team")
        }
      }
    }
  }
}

signing {
  val signingKeyId: String? by project
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
  setRequired {
    project.signingCondition()
  }
  sign(publishing.publications)
}

tasks.withType<Sign> {
  onlyIf {
    val artifactorySkip: Boolean = tasks.artifactoryPublish.get().skip
    !artifactorySkip && project.signingCondition()
  }
}

// `afterEvaluate` is required to inject configurable properties; see https://github.com/jfrog/artifactory-gradle-plugin/issues/71#issuecomment-1734977528
project.afterEvaluate {
  artifactory {
    if (artifactoryConfiguration.artifactsToPublish.isPresent) {
      clientConfig.info.addEnvironmentProperty(
        "ARTIFACTS_TO_PUBLISH",
        artifactoryConfiguration.artifactsToPublish.get(),
      )
      clientConfig.info.addEnvironmentProperty(
        "ARTIFACTS_TO_DOWNLOAD",
        artifactoryConfiguration.artifactsToDownload.getOrElse(""),
      )
    }

    setContextUrl(System.getenv("ARTIFACTORY_URL"))
    // Note: `publish` should only be called once: https://github.com/jfrog/artifactory-gradle-plugin/issues/111
    publish {
      if (artifactoryConfiguration.repoKeyEnv.isPresent) {
        repository {
          repoKey = System.getenv(artifactoryConfiguration.repoKeyEnv.get())
          username = System.getenv(artifactoryConfiguration.usernameEnv.get())
          password = System.getenv(artifactoryConfiguration.passwordEnv.get())
        }
      }
      defaults {
        publications("mavenJava")
        setProperties(
          mapOf(
            "build.name" to "sonar-php",
            "version" to project.version.toString(),
            "build.number" to project.ext["buildNumber"].toString(),
            "pr.branch.target" to System.getenv("PULL_REQUEST_BRANCH_TARGET"),
            "pr.number" to System.getenv("PULL_REQUEST_NUMBER"),
            "vcs.branch" to System.getenv("GIT_BRANCH"),
            "vcs.revision" to System.getenv("GIT_COMMIT"),
          ),
        )
        setPublishArtifacts(true)
        setPublishPom(true)
        setPublishIvy(false)
      }
    }

    clientConfig.info.addEnvironmentProperty("PROJECT_VERSION", project.version.toString())
    clientConfig.info.buildName = "sonar-php"
    clientConfig.info.buildNumber = project.ext["buildNumber"].toString()
    clientConfig.isIncludeEnvVars = true
    clientConfig.envVarsExcludePatterns =
      "*password*,*PASSWORD*,*secret*,*MAVEN_CMD_LINE_ARGS*,sun.java.command," +
      "*token*,*TOKEN*,*LOGIN*,*login*,*key*,*KEY*,*PASSPHRASE*,*signing*"
  }
}
