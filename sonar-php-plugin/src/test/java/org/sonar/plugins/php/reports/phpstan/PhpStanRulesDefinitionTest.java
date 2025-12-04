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
package org.sonar.plugins.php.reports.phpstan;

import java.util.Map;
import javax.annotation.Nullable;
import org.sonar.api.SonarRuntime;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.rules.CleanCodeAttribute;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.php.reports.AbstractExternalRulesDefinition;
import org.sonar.plugins.php.reports.AbstractExternalRulesDefinitionTest;

import static org.assertj.core.api.Assertions.assertThat;

class PhpStanRulesDefinitionTest extends AbstractExternalRulesDefinitionTest {

  @Override
  protected void customRuleAssertion(RulesDefinition.Repository repository, boolean shouldSupportCCT) {
    RulesDefinition.Rule rule = repository.rule("phpstan.finding");
    assertThat(rule).isNotNull();
    assertThat(rule.name()).isEqualTo("PHPStan Rule");
    assertThat(rule.htmlDescription()).isEqualTo("This is external rule <code>phpstan:phpstan.finding</code>. No details are available.");
    assertThat(rule.tags()).isEmpty();

    if (shouldSupportCCT) {
      assertThat(rule.cleanCodeAttribute()).isEqualTo(CleanCodeAttribute.LOGICAL);
      assertThat(rule.defaultImpacts()).containsOnly(Map.entry(SoftwareQuality.RELIABILITY, Severity.MEDIUM));
    } else {
      // if CCT is not supported, the defaults will apply
      assertThat(rule.cleanCodeAttribute()).isNull();
      assertThat(rule.defaultImpacts()).containsOnly(Map.entry(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM));
    }
  }

  @Override
  protected AbstractExternalRulesDefinition rulesDefinition(@Nullable SonarRuntime sonarRuntime) {
    return new PhpStanRulesDefinition(sonarRuntime);
  }

  @Override
  protected int numberOfRules() {
    return 1;
  }

  @Override
  protected String reportName() {
    return PhpStanSensor.PHPSTAN_REPORT_NAME;
  }

  @Override
  protected String reportKey() {
    return PhpStanSensor.PHPSTAN_REPORT_KEY;
  }
}
