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
package org.sonar.php.checks.wordpress;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S6343")
public class WordPressAutoUpdateCheck extends WordPressConfigVisitor {

  private static final String MESSAGE = "Make sure deactivating automatic updates is intended.";

  @Override
  protected Set<String> configsToVisit() {
    return new HashSet<>(Arrays.asList("AUTOMATIC_UPDATER_DISABLED", "WP_AUTO_UPDATE_CORE", "DISALLOW_FILE_MODS"));
  }

  @Override
  void visitConfigDeclaration(FunctionCallTree config) {
    configKeyString(config).ifPresent(key -> {
      if ("WP_AUTO_UPDATE_CORE".equals(key)) {
        raiseOnMatchingValue(config, CheckUtils::isFalseValue);
      } else {
        // AUTOMATIC_UPDATER_DISABLED or DISALLOW_FILE_MODS
        raiseOnMatchingValue(config, CheckUtils::isTrueValue);
      }
    });
  }

  private void raiseOnMatchingValue(FunctionCallTree config, Predicate<ExpressionTree> valuePredicate) {
    configValue(config).filter(valuePredicate).ifPresent(v -> newIssue(config, MESSAGE));
  }
}
