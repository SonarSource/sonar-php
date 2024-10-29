plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.jfrog.buildinfo.gradle)
    implementation(libs.sonar.scanner.gradle)
    implementation(libs.diffplug.spotless)
    implementation(libs.diffplug.blowdryer)
    implementation(libs.commons.io)
}
