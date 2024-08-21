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
package org.sonar.plugins.php.reports.psalm;

import java.util.Map;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.sonar.api.SonarRuntime;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.php.reports.ExternalRulesDefinitionTest;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

import static org.assertj.core.api.Assertions.assertThat;

class PsalmRuleDefinitionTest extends ExternalRulesDefinitionTest {

  @BeforeEach
  void init() {
    PsalmRulesDefinition.setRuleLoader(null);
  }

  @Override
  protected void customRuleAssertion(RulesDefinition.Repository repository) {
    RulesDefinition.Rule rule = repository.rule("AbstractMethodCall");
    assertThat(rule).isNotNull();
    assertThat(rule.name()).isEqualTo("AbstractMethodCall");
    assertThat(rule.tags()).isEmpty();

    // TODO: SONARPHP-1525 should add branching based on isCCTSupported for these assertions
    assertThat(rule.cleanCodeAttribute()).isNull();
    assertThat(rule.defaultImpacts()).containsOnly(Map.entry(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM));
  }

  @Override
  protected ExternalRuleLoader ruleLoader() {
    return PsalmRulesDefinition.ruleLoader();
  }

  @Override
  protected RulesDefinition rulesDefinition(@Nullable SonarRuntime sonarRuntime) {
    return new PsalmRulesDefinition(sonarRuntime);
  }

  @Override
  protected int numberOfRules() {
    return 266;
  }

  @Override
  protected String reportName() {
    return PsalmSensor.PSALM_REPORT_NAME;
  }

  @Override
  protected String reportKey() {
    return PsalmSensor.PSALM_REPORT_KEY;
  }
}
