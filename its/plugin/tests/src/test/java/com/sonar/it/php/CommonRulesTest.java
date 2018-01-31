/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sonar.it.php;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import java.io.File;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.issue.IssueQuery;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonRulesTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final File PROJECT_DIR = Tests.projectDirectoryFor("common-rules");

  private static final String SOURCE_DIR = "src";
  private static final String TESTS_DIR = "tests";
  private static final String REPORTS_DIR = "reports";

  @BeforeClass
  public static void startServer() throws Exception {
    orchestrator.resetData();

    createReportsWithAbsolutePath();

    SonarScanner build = SonarScanner.create()
      .setProjectDir(PROJECT_DIR)
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1.0")
      .setSourceDirs(SOURCE_DIR)
      .setTestDirs(TESTS_DIR)
      .setProperty("sonar.php.coverage.reportPath", REPORTS_DIR + "/.coverage-with-absolute-path.xml")
      .setProperty("sonar.php.tests.reportPath", REPORTS_DIR + "/.tests-with-absolute-path.xml")
      .setProfile("it-profile");

    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
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
    assertThat(
      orchestrator.getServer().wsClient().issueClient().find(IssueQuery.create().componentRoots("project").severities("BLOCKER").rules("php:S3334")).list())
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
      Files.toString(new File(PROJECT_DIR, REPORTS_DIR + "/phpunit.coverage.xml"), Charsets.UTF_8)
        .replace("Math.php", new File(PROJECT_DIR, SOURCE_DIR + "/Math.php").getAbsolutePath()),
      new File(PROJECT_DIR, REPORTS_DIR + "/.coverage-with-absolute-path.xml"), Charsets.UTF_8);

    Files.write(
      Files.toString(new File(PROJECT_DIR, REPORTS_DIR + "/phpunit.xml"), Charsets.UTF_8)
        .replace("SomeTest.php", new File(PROJECT_DIR, TESTS_DIR + "/SomeTest.php").getAbsolutePath()),
      new File(PROJECT_DIR, REPORTS_DIR + "/.tests-with-absolute-path.xml"), Charsets.UTF_8);
  }

}
