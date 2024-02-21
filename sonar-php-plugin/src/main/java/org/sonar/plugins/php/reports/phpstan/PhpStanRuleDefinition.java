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
package org.sonar.plugins.php.reports.phpstan;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.php.api.Php;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

import static org.sonar.plugins.php.reports.phpstan.PhpStanSensor.PHPSTAN_REPORT_KEY;
import static org.sonar.plugins.php.reports.phpstan.PhpStanSensor.PHPSTAN_REPORT_NAME;

public class PhpStanRuleDefinition implements RulesDefinition {

  private static final String RULES_JSON = "org/sonar/plugins/php/reports/phpstan/rules.json";

  static final ExternalRuleLoader RULE_LOADER = new ExternalRuleLoader(PHPSTAN_REPORT_KEY, PHPSTAN_REPORT_NAME, RULES_JSON, Php.KEY);

  @Override
  public void define(Context context) {
    RULE_LOADER.createExternalRuleRepository(context);
  }
}
