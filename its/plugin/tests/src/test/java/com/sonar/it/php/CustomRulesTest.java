/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2023 SonarSource SA
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
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarqube.ws.Issues.Issue;

class CustomRulesTest extends Tests {

//  @RegisterExtension
//  public static OrchestratorExtension orchestrator = Tests.ORCHESTRATOR;
  private static final String PROJECT_KEY = "custom-rules";
  private static final String PROJECT_NAME = "Custom Rules";
  private static List<Issue> issues;

  @BeforeAll
  static void prepare() {
    Tests.provisionProject(PROJECT_KEY, PROJECT_NAME, "php", "php-custom-rules-profile");
    SonarScanner build = SonarScanner.create()
      .setProjectDir(Tests.projectDirectoryFor("custom_rules"))
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_NAME)
      .setProjectVersion("1.0")
      .setSourceDirs("src");
    Tests.executeBuildWithExpectedWarnings(ORCHESTRATOR, build);
    issues = Tests.issuesForComponent(PROJECT_KEY);
  }

  @Test
  void baseTreeVisitorCheck() {
    assertSingleIssue("php-custom-rules:visitor", 5, "Function expression.", "5min");
  }

  @Test
  void subscriptionBaseVisitorCheck() {
    assertSingleIssue("php-custom-rules:subscription", 8, "For statement.", "10min");
  }

  private void assertSingleIssue(String ruleKey, int expectedLine, String expectedMessage, String expectedDebt) {
    assertThat(Tests.issuesForRule(issues, ruleKey)).hasSize(1);
    Issue issue = Tests.issuesForRule(issues, ruleKey).get(0);
    assertThat(issue.getLine()).isEqualTo(expectedLine);
    assertThat(issue.getMessage()).isEqualTo(expectedMessage);
    assertThat(issue.getDebt()).isEqualTo(expectedDebt);
  }

}
