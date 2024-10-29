package org.sonarsource.php

import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPomLicense

interface ArtifactoryConfiguration {
  val artifactsToPublish: Property<String>
  val artifactsToDownload: Property<String>
  val repoKeyEnv: Property<String>
  val usernameEnv: Property<String>
  val passwordEnv: Property<String>
  var license: (MavenPomLicense.() -> Unit)?
  fun license(action: MavenPomLicense.() -> Unit) {
    license = action
  }
}
