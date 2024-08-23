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
package org.sonar.plugins.php.reports;

import javax.annotation.Nullable;
import org.sonar.api.SonarRuntime;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.php.api.Php;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

@ScannerSide
public class ExternalRulesDefinition implements RulesDefinition {
  private static final String RULES_JSON_PATH = "org/sonar/plugins/php/reports/%s/rules.json";
  private final ExternalRuleLoader ruleLoader;

  public ExternalRulesDefinition(@Nullable SonarRuntime sonarRuntime, String reportKey, String reportName) {
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
