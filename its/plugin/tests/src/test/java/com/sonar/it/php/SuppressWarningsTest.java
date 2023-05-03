package com.sonar.it.php;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.Issues;

import static org.assertj.core.api.Assertions.assertThat;

public class SuppressWarningsTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final String PROJECT_KEY = "suppress_warnings";

  @Test
  public void shouldSuppressIssueWithAnnotation() {
    Tests.provisionProject(PROJECT_KEY, "SuppressWarningsTest", "php", "nosonar-profile");
    SonarScanner build = SonarScanner.create()
      .setProjectDir(Tests.projectDirectoryFor(PROJECT_KEY));
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
    List<Issues.Issue> issues = Tests.issuesForComponent(PROJECT_KEY);
    assertThat(issues).isEmpty();
  }

}
