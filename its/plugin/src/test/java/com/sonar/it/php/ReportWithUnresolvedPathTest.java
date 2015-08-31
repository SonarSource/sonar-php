/*
 * Copyright (C) 2011-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.it.php;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.SonarRunner;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.File;
import java.util.regex.Pattern;

import static org.fest.assertions.Assertions.assertThat;

public class ReportWithUnresolvedPathTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final File projectDir = new File("projects/phpunit/");

  @Test
  public void should_log_a_warning() throws Exception {
    Assume.assumeTrue(Tests.is_after_plugin("2.6"));
    orchestrator.resetData();
    SonarRunner build = SonarRunner.create()
      .setProjectDir(projectDir)
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1.0")
      .setSourceDirs("src")
      .setTestDirs("tests")
      .setProperty("sonar.php.coverage.reportPath", "reports/phpunit.coverage.xml");
    BuildResult result = orchestrator.executeBuild(build);
    String logs = result.getLogs();
    String expected = "WARN.*Could not resolve 1 file paths in phpunit.coverage.xml, first unresolved path: Math\\.php";
    assertThat(Pattern.compile(expected).matcher(logs).find()).isTrue();
  }

}
