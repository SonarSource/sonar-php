/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;

@Rule(key = "S6347")
public class WordPressLateConfigCheck extends WordPressConfigVisitor {

  private static final String MESSAGE = "Configuration options at this location will not be taken into account.";
  private boolean endOfConfig;

  @Override
  public void visitScript(ScriptTree tree) {
    endOfConfig = false;
    super.visitScript(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    checkEndOfConfig(tree);
    super.visitFunctionCall(tree);
  }

  @Override
  void visitConfigDeclaration(FunctionCallTree config) {
    if (endOfConfig) {
      newIssue(config, MESSAGE);
    }
  }

  private void checkEndOfConfig(FunctionCallTree tree) {
    if ("require_once".equalsIgnoreCase(CheckUtils.functionName(tree)) && isSettingsInclusion(tree)) {
      endOfConfig = true;
    }
  }

  private static boolean isSettingsInclusion(FunctionCallTree tree) {
    return CheckUtils.argument(tree, "", 0)
      .map(CallArgumentTree::value)
      .map(WordPressLateConfigCheck::extractRelativePath)
      .filter(a -> CheckUtils.isStringLiteralWithValue(a, "wp-settings.php"))
      .isPresent();
  }

  private static ExpressionTree extractRelativePath(ExpressionTree argument) {
    if (argument.is(Tree.Kind.PARENTHESISED_EXPRESSION)) {
      argument = ((ParenthesisedExpressionTree) argument).expression();
    }
    return argument.is(Tree.Kind.CONCATENATION) ? ((BinaryExpressionTree) argument).rightOperand() : null;
  }
}
