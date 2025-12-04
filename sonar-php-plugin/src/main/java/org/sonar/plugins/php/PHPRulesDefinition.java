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
package org.sonar.plugins.php;

import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.php.checks.CheckList;
import org.sonar.plugins.php.api.Php;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public class PHPRulesDefinition implements RulesDefinition {

  private static final String REPOSITORY_NAME = "SonarAnalyzer";
  static final String RESOURCE_BASE_PATH = "org/sonar/l10n/php/rules/php";
  private final SonarRuntime runtime;

  public PHPRulesDefinition(SonarRuntime runtime) {
    this.runtime = runtime;
  }

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(CheckList.REPOSITORY_KEY, Php.KEY).setName(REPOSITORY_NAME);

    RuleMetadataLoader ruleMetadataLoader = new RuleMetadataLoader(RESOURCE_BASE_PATH, PHPProfileDefinition.SONAR_WAY_PATH, runtime);

    ruleMetadataLoader.addRulesByAnnotatedClass(repository, CheckList.getAllChecks());

    repository.done();
  }
}
