/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import com.sonar.orchestrator.build.SonarScanner;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarqube.ws.Issues.Issue;

class CustomRulesTest extends OrchestratorTest {

  private static final String PROJECT_KEY = "custom-rules";
  private static final String PROJECT_NAME = "Custom Rules";
  private static List<Issue> issues;

  @BeforeAll
  static void prepare() {
    provisionProject(PROJECT_KEY, PROJECT_NAME, "php", "php-custom-rules-profile");
    SonarScanner build = createScanner()
      .setProjectDir(projectDirectoryFor("custom_rules"))
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_NAME);
    executeBuildWithExpectedWarnings(ORCHESTRATOR, build);
    issues = issuesForComponent(PROJECT_KEY);
  }

  @Test
  void baseTreeVisitorCheck() {
    assertSingleIssue("custom:S1", 4, "Remove the usage of this forbidden function.", "5min");
  }

  @Test
  void subscriptionBaseVisitorCheck() {
    assertSingleIssue("custom:S2", 6, "Remove the usage of this other forbidden function.", "10min");
  }

  private static void assertSingleIssue(String ruleKey, int expectedLine, String expectedMessage, String expectedDebt) {
    assertThat(issuesForRule(issues, ruleKey)).hasSize(1);
    var issue = issuesForRule(issues, ruleKey).get(0);
    assertThat(issue.getLine()).isEqualTo(expectedLine);
    assertThat(issue.getMessage()).isEqualTo(expectedMessage);
    assertThat(issue.getDebt()).isEqualTo(expectedDebt);
  }

}
