package com.sonar.it.php;

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.sonar.it.php.Tests.createScanner;

public class CpdTokenTest {

  @RegisterExtension
  public static OrchestratorExtension orchestrator = Tests.ORCHESTRATOR;

  private static final String PROJECT = "php8-features";

  @Test
  void supportPhp8Features() {
    Tests.provisionProject(PROJECT, PROJECT, "php", "it-profile");
    SonarScanner build = createScanner()
      .setProjectKey(PROJECT)
      .setProjectName(PROJECT)
      .setProjectDir(Tests.projectDirectoryFor(PROJECT));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
  }
}
