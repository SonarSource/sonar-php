package org.sonarsource.php

import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.file.RegularFileProperty

interface CodeStyleConvention {
  val licenseHeaderFile: RegularFileProperty
  var spotless: (SpotlessExtension.() -> Unit)?
  fun spotless(action: SpotlessExtension.() -> Unit) {
    spotless = action
  }
}
