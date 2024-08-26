/*
 * SonarQube PHP Plugin
 * Copyright (C) 2011-2024 SonarSource SA
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
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;

import static com.sonar.it.php.Tests.createScanner;
import static org.assertj.core.api.Assertions.assertThat;

class PsalmReportTest {

  private static final String PROJECT = "psalm_project";

  @RegisterExtension
  public static final OrchestratorExtension ORCHESTRATOR = Tests.ORCHESTRATOR;

  @Test
  void importReport() {
    Tests.provisionProject(PROJECT, PROJECT, "php", "no_rules");
    SonarScanner build = createScanner()
      .setProjectDir(Tests.projectDirectoryFor(PROJECT));
    Tests.executeBuildWithExpectedWarnings(ORCHESTRATOR, build);

    List<Issues.Issue> issues = Tests.issuesForComponent(PROJECT);
    assertThat(issues).hasSize(2);
    Issues.Issue first = issues.get(0);
    assertThat(first.getComponent()).isEqualTo("psalm_project:src/test.php");
    assertThat(first.getRule()).isEqualTo("external_psalm:psalm.finding");
    assertThat(first.getMessage()).isEqualTo("Second issue on test.php");
    assertThat(first.getImpactsList()).hasSize(1);

    Common.Impact firstImpact = first.getImpactsList().get(0);
    // TODO: SONARPHP-1496 Verify Issue Impacts are correctly send to Sonarqube
    // Should be RELIABILITY and HIGH
    assertThat(firstImpact.getSoftwareQuality()).isEqualTo(Common.SoftwareQuality.MAINTAINABILITY);
    assertThat(firstImpact.getSeverity()).isEqualTo(Common.ImpactSeverity.MEDIUM);

    assertThat(first.getCleanCodeAttribute()).isEqualTo(Common.CleanCodeAttribute.CONVENTIONAL);
    assertThat(first.getType()).isEqualTo(Common.RuleType.BUG);
    assertThat(first.getSeverity()).isEqualTo(Common.Severity.CRITICAL);
    assertThat(first.getEffort()).isEqualTo("5min");
    Common.TextRange firstTextRange = first.getTextRange();
    assertThat(firstTextRange).isNotNull();
    assertThat(firstTextRange.getStartLine()).isEqualTo(2);
    assertThat(firstTextRange.getStartOffset()).isEqualTo(1);
    assertThat(firstTextRange.getEndLine()).isEqualTo(2);
    assertThat(firstTextRange.getEndOffset()).isEqualTo(10);

    Issues.Issue second = issues.get(1);
    assertThat(second.getComponent()).isEqualTo("psalm_project:src/test.php");
    assertThat(second.getRule()).isEqualTo("external_psalm:InvalidScalarArgument");
    assertThat(second.getMessage()).isEqualTo("First issue on test.php");
    assertThat(second.getImpactsList()).hasSize(1);

    Common.Impact secondImpact = second.getImpactsList().get(0);
    // TODO: SONARPHP-1496 Verify Issue Impacts are correctly send to Sonarqube
    // Should be RELIABILITY and HIGH
    assertThat(secondImpact.getSoftwareQuality()).isEqualTo(Common.SoftwareQuality.RELIABILITY);
    assertThat(secondImpact.getSeverity()).isEqualTo(Common.ImpactSeverity.HIGH);
    assertThat(second.getCleanCodeAttribute()).isEqualTo(Common.CleanCodeAttribute.LOGICAL);
    assertThat(second.getType()).isEqualTo(Common.RuleType.BUG);
    assertThat(second.getSeverity()).isEqualTo(Common.Severity.CRITICAL);
    assertThat(second.getEffort()).isEqualTo("5min");
    Common.TextRange secondTextRange = second.getTextRange();
    assertThat(secondTextRange).isNotNull();
    assertThat(secondTextRange.getStartLine()).isEqualTo(5);
    assertThat(secondTextRange.getStartOffset()).isEqualTo(4);
    assertThat(secondTextRange.getEndLine()).isEqualTo(5);
    assertThat(secondTextRange.getEndOffset()).isEqualTo(16);
  }
}
