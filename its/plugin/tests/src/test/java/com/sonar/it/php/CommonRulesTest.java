/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2022 SonarSource SA
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

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import java.io.File;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.Issues;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonRulesTest {

  @ClassRule
  public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

  private static final File PROJECT_DIR = Tests.projectDirectoryFor("common-rules");
  private static final String PROJECT_KEY = "common-rules";
  private static final String PROJECT_NAME = "Common Rules";

  private static final String SOURCE_DIR = "src";
  private static final String TESTS_DIR = "tests";
  private static final String REPORTS_DIR = "reports";

  @BeforeClass
  public static void startServer() throws Exception {
    Tests.provisionProject(PROJECT_KEY, PROJECT_NAME, "php", "it-profile");
    SonarScanner build = SonarScanner.create()
      .setProjectDir(PROJECT_DIR)
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_NAME)
      .setProjectVersion("1.0")
      .setSourceDirs(SOURCE_DIR)
      .setTestDirs(TESTS_DIR)
      .setProperty("sonar.php.coverage.reportPaths", REPORTS_DIR + "/phpunit.coverage.xml")
      .setProperty("sonar.php.tests.reportPath", REPORTS_DIR + "/phpunit.xml");

    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
  }

  @Test
  public void tests() throws Exception {
    List<Issues.Issue> issues = Tests.issuesForComponent(PROJECT_KEY);

    assertThat(Tests.issuesForRule(issues,"common-php:DuplicatedBlocks")).hasSize(2);
    assertThat(Tests.issuesForRule(issues,"common-php:InsufficientCommentDensity")).hasSize(2);
    assertThat(Tests.issuesForRule(issues,"common-php:FailedUnitTests")).hasSize(1);
    assertThat(Tests.issuesForRule(issues,"common-php:InsufficientLineCoverage")).hasSize(1);
    assertThat(Tests.issuesForRule(issues,"php:S3334")).hasSize(1);
  }

}
