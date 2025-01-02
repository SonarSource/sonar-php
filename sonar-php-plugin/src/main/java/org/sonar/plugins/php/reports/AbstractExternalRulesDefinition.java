/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.plugins.php.reports;

import javax.annotation.Nullable;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.php.api.Php;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

public abstract class AbstractExternalRulesDefinition implements RulesDefinition {
  private static final String RULES_JSON_PATH = "org/sonar/plugins/php/reports/%s/rules.json";
  private final ExternalRuleLoader ruleLoader;

  protected AbstractExternalRulesDefinition(@Nullable SonarRuntime sonarRuntime, String reportKey, String reportName) {
    this.ruleLoader = new ExternalRuleLoader(reportKey, reportName, RULES_JSON_PATH.formatted(reportKey), Php.KEY, sonarRuntime);
  }

  @Override
  public void define(Context context) {
    ruleLoader.createExternalRuleRepository(context);
  }

  public ExternalRuleLoader getRuleLoader() {
    return ruleLoader;
  }
}
