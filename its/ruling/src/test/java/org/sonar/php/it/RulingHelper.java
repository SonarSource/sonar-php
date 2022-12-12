package org.sonar.php.it;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.OrchestratorBuilder;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.container.Edition;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.MavenLocation;
import java.io.File;
import java.io.IOException;

public class RulingHelper {

  private static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  private static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE";

  static Orchestrator getOrchestrator(Edition sonarEdition) {
    OrchestratorBuilder builder = Orchestrator.builderEnv()
      .useDefaultAdminCredentialsForBuilds(true)
      .setSonarVersion(System.getProperty(SQ_VERSION_PROPERTY, DEFAULT_SQ_VERSION))
      .setEdition(sonarEdition)
      .addPlugin(FileLocation.byWildcardMavenFilename(new File("../../sonar-php-plugin/target"), "sonar-php-plugin-*.jar"))
      .addPlugin(MavenLocation.of("org.sonarsource.sonar-lits-plugin", "sonar-lits-plugin", "0.10.0.2181"));

    if (sonarEdition != Edition.COMMUNITY) {
      builder.activateLicense();
    }

    return builder.build();
  }

  static Orchestrator getOrchestrator() {
    return getOrchestrator(Edition.COMMUNITY);
  }

  static SonarScanner prepareScanner(File path, String projectKey, String expectedIssueLocation, File litsDifferencesFile) throws IOException {
    return SonarScanner.create(path)
      .setProjectKey(projectKey)
      .setProjectName(projectKey)
      .setProjectVersion("1")
      .setLanguage("php")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProperty("sonar.lits.dump.old", FileLocation.of("src/test/resources/" + expectedIssueLocation).getFile().getAbsolutePath())
      .setProperty("sonar.lits.dump.new", FileLocation.of("target/actual").getFile().getAbsolutePath())
      .setProperty("sonar.cpd.exclusions", "**/*")
      .setProperty("sonar.lits.differences", litsDifferencesFile.getAbsolutePath())
      .setProperty("sonar.internal.analysis.failFast", "true")
      .setEnvironmentVariable("SONAR_RUNNER_OPTS", "-Xmx2000m");
  }
}
