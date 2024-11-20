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
package org.sonar.php.checks;

import java.util.Arrays;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = LogicalWordOperatorUsageCheck.KEY)
public class LogicalWordOperatorUsageCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S2010";
  public static final String MESSAGE = "Replace \"%s\" with \"%s\".";

  @Override
  public List<Kind> nodesToVisit() {
    return Arrays.asList(
      Kind.ALTERNATIVE_CONDITIONAL_AND,
      Kind.ALTERNATIVE_CONDITIONAL_OR);
  }

  @Override
  public void visitNode(Tree tree) {
    BinaryExpressionTree binaryExpressionTree = (BinaryExpressionTree) tree;
    if (isCallToDie(binaryExpressionTree.rightOperand())) {
      return;
    }

    SyntaxToken operator = binaryExpressionTree.operator();
    String replacement = tree.is(Kind.ALTERNATIVE_CONDITIONAL_AND)
      ? PHPPunctuator.ANDAND.getValue()
      : PHPPunctuator.OROR.getValue();

    context().newIssue(this, operator, String.format(MESSAGE, operator.text(), replacement));
  }

  private static boolean isCallToDie(ExpressionTree tree) {
    return tree.is(Kind.FUNCTION_CALL) && "die".equals(CheckUtils.getLowerCaseFunctionName((FunctionCallTree) tree));
  }
}
