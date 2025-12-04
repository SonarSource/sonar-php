/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package com.sonar.it.php;

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.sonar.it.php.Tests.createScanner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarqube.ws.Issues.Issue;

class CustomRulesTest {

  @RegisterExtension
  public static final OrchestratorExtension orchestrator = Tests.ORCHESTRATOR;
  private static final String PROJECT_KEY = "custom-rules";
  private static final String PROJECT_NAME = "Custom Rules";
  private static List<Issue> issues;

  @BeforeAll
  static void prepare() {
    Tests.provisionProject(PROJECT_KEY, PROJECT_NAME, "php", "php-custom-rules-profile");
    SonarScanner build = createScanner()
      .setProjectDir(Tests.projectDirectoryFor("custom_rules"))
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_NAME);
    Tests.executeBuildWithExpectedWarnings(orchestrator, build);
    issues = Tests.issuesForComponent(PROJECT_KEY);
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
    assertThat(Tests.issuesForRule(issues, ruleKey)).hasSize(1);
    var issue = Tests.issuesForRule(issues, ruleKey).get(0);
    assertThat(issue.getLine()).isEqualTo(expectedLine);
    assertThat(issue.getMessage()).isEqualTo(expectedMessage);
    assertThat(issue.getDebt()).isEqualTo(expectedDebt);
  }

}
