/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.plugins.php;

import java.util.ArrayList;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.php.checks.CheckList;
import org.sonar.plugins.php.api.Php;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public class PHPRulesDefinition implements RulesDefinition {

  private static final String REPOSITORY_NAME = "SonarAnalyzer";
  static final String RESOURCE_BASE_PATH = "org/sonar/l10n/php/rules/php";
  private final SonarRuntime sonarRuntime;

  public PHPRulesDefinition(SonarRuntime sonarRuntime) {
    this.sonarRuntime = sonarRuntime;
  }

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(CheckList.REPOSITORY_KEY, Php.KEY).setName(REPOSITORY_NAME);

    RuleMetadataLoader ruleMetadataLoader = new RuleMetadataLoader(RESOURCE_BASE_PATH, PHPProfileDefinition.SONAR_WAY_PATH, sonarRuntime);

    ruleMetadataLoader.addRulesByAnnotatedClass(repository, new ArrayList<>(CheckList.getAllChecks()));

    repository.done();
  }
}
