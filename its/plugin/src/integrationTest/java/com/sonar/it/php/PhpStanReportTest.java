/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;

import static com.sonar.it.php.Tests.createScanner;
import static org.assertj.core.api.Assertions.assertThat;

class PhpStanReportTest {

  private static final String PROJECT = "phpstan_project";

  @RegisterExtension
  public static final OrchestratorExtension ORCHESTRATOR = Tests.ORCHESTRATOR;

  @Test
  void importReport() {
    Tests.provisionProject(PROJECT, PROJECT, "php", "no_rules");
    SonarScanner build = createScanner()
      .setProjectDir(Tests.projectDirectoryFor("phpstan_project"));
    Tests.executeBuildWithExpectedWarnings(ORCHESTRATOR, build);

    List<Issues.Issue> issues = Tests.issuesForComponent("phpstan_project");
    assertThat(issues).hasSize(2);
    Issues.Issue first = issues.get(0);
    assertThat(first.getComponent()).isEqualTo("phpstan_project:src/test.php");
    assertThat(first.getRule()).isEqualTo("external_phpstan:phpstan.finding");
    assertThat(first.getMessage()).isEqualTo("Message for issue without line.");
    assertThat(first.getImpactsList()).hasSize(1);

    Common.Impact firstImpact = first.getImpactsList().get(0);
    assertThat(firstImpact.getSoftwareQuality()).isEqualTo(Common.SoftwareQuality.RELIABILITY);
    assertThat(firstImpact.getSeverity()).isEqualTo(Common.ImpactSeverity.MEDIUM);
    assertThat(first.getCleanCodeAttribute()).isEqualTo(Common.CleanCodeAttribute.LOGICAL);
    assertThat(first.getType()).isEqualTo(Common.RuleType.CODE_SMELL);
    assertThat(first.getSeverity()).isEqualTo(Common.Severity.MAJOR);
    assertThat(first.getEffort()).isEqualTo("5min");
    assertThat(first.getLine()).isZero();

    Issues.Issue second = issues.get(1);
    assertThat(second.getComponent()).isEqualTo("phpstan_project:src/test.php");
    assertThat(second.getRule()).isEqualTo("external_phpstan:phpstan.finding");
    assertThat(second.getMessage()).isEqualTo("Parameter #1 $i of function foo expects int, string given.");
    assertThat(second.getImpactsList()).hasSize(1);

    Common.Impact secondImpact = second.getImpactsList().get(0);
    assertThat(secondImpact.getSoftwareQuality()).isEqualTo(Common.SoftwareQuality.RELIABILITY);
    assertThat(secondImpact.getSeverity()).isEqualTo(Common.ImpactSeverity.MEDIUM);
    assertThat(second.getCleanCodeAttribute()).isEqualTo(Common.CleanCodeAttribute.LOGICAL);
    assertThat(second.getType()).isEqualTo(Common.RuleType.CODE_SMELL);
    assertThat(second.getSeverity()).isEqualTo(Common.Severity.MAJOR);
    assertThat(second.getEffort()).isEqualTo("5min");
    assertThat(second.getLine()).isEqualTo(5);
  }
}
