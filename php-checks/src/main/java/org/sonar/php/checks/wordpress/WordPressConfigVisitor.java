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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;

public abstract class WordPressConfigVisitor extends FunctionUsageCheck {
  @Override
  protected Set<String> lookedUpFunctionNames() {
    return Collections.singleton("define");
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    if (isWpConfigFile()) {
      super.visitCompilationUnit(tree);
    }
  }

  @Override
  protected void checkFunctionCall(FunctionCallTree tree) {
    if (shouldVisitConfig(tree)) {
      visitConfigDeclaration(tree);
    }
  }

  private boolean isWpConfigFile() {
    return context().getPhpFile().filename().equals("wp-config.php");
  }

  private boolean shouldVisitConfig(FunctionCallTree tree) {
    return configsToVisit().isEmpty() || configsToVisit().stream().anyMatch(configKey -> isConfigKey(tree, configKey));
  }

  protected static Optional<ExpressionTree> configKey(FunctionCallTree tree) {
    return CheckUtils.argument(tree, "constant_name", 0).map(CallArgumentTree::value);
  }

  protected Optional<String> configKeyString(FunctionCallTree config) {
    return configKey(config)
      .filter(c -> c.is(Tree.Kind.REGULAR_STRING_LITERAL))
      .map(c -> CheckUtils.trimQuotes((LiteralTree) c));
  }

  protected static Optional<ExpressionTree> configValue(FunctionCallTree tree) {
    return CheckUtils.argument(tree, "value", 1).map(CallArgumentTree::value);
  }

  public static boolean isConfigKey(FunctionCallTree tree, String key) {
    return configKey(tree).filter(argument -> CheckUtils.isStringLiteralWithValue(argument, key)).isPresent();
  }

  protected Set<String> configsToVisit() {
    return Collections.emptySet();
  }

  abstract void visitConfigDeclaration(FunctionCallTree config);
}
