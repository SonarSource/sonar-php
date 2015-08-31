/*
 * Copyright (C) 2011-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.it.php;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarRunner;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.issue.IssueQuery;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class CommonRulesTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final File projectDir = new File("projects/common-rules/");

  private static final String SOURCE_DIR = "src";
  private static final String TESTS_DIR = "tests";
  private static final String REPORTS_DIR = "reports";

  @BeforeClass
  public static void sspotartServer() throws Exception {
    Assume.assumeTrue(Tests.is_after_plugin("2.5"));

    orchestrator.resetData();

    createReportsWithAbsolutePath();

    SonarRunner build = SonarRunner.create()
      .setProjectDir(projectDir)
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1.0")
      .setSourceDirs(SOURCE_DIR)
      .setTestDirs(TESTS_DIR)
      .setProperty("sonar.php.coverage.reportPath", REPORTS_DIR + "/.coverage-with-absolute-path.xml")
      .setProperty("sonar.php.tests.reportPath", REPORTS_DIR + "/.tests-with-absolute-path.xml")
      .setProfile("it-profile");

    orchestrator.executeBuild(build);
  }

  @Test
  public void tests() throws Exception {
    assertThat(orchestrator.getServer().wsClient().issueClient().find(IssueQuery.create().componentRoots("project").severities("INFO").rules("common-php:DuplicatedBlocks")).list())
      .hasSize(2);
    assertThat(
      orchestrator.getServer().wsClient().issueClient().find(IssueQuery.create().componentRoots("project").severities("INFO").rules("common-php:InsufficientCommentDensity"))
        .list()).hasSize(2);
    assertThat(orchestrator.getServer().wsClient().issueClient().find(IssueQuery.create().componentRoots("project").severities("INFO").rules("common-php:FailedUnitTests")).list())
      .hasSize(1);
    assertThat(
      orchestrator.getServer().wsClient().issueClient().find(IssueQuery.create().componentRoots("project").severities("INFO").rules("common-php:InsufficientLineCoverage")).list())
      .hasSize(1);
  }

  /**
   * Replace file name with absolute path in test and coverage report.
   * <p/>
   * This hack allow to have this integration test, as only absolute path
   * in report is supported.
   */
  private static void createReportsWithAbsolutePath() throws Exception {
    Files.write(
      Files.toString(new File(projectDir, REPORTS_DIR + "/phpunit.coverage.xml"), Charsets.UTF_8)
        .replace("Math.php", new File(projectDir, SOURCE_DIR + "/Math.php").getAbsolutePath()),
      new File(projectDir, REPORTS_DIR + "/.coverage-with-absolute-path.xml"), Charsets.UTF_8);

    Files.write(
      Files.toString(new File(projectDir, REPORTS_DIR + "/phpunit.xml"), Charsets.UTF_8)
        .replace("SomeTest.php", new File(projectDir, TESTS_DIR + "/SomeTest.php").getAbsolutePath()),
      new File(projectDir, REPORTS_DIR + "/.tests-with-absolute-path.xml"), Charsets.UTF_8);
  }

}
