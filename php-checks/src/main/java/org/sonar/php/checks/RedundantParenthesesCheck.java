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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ParenthesisedExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = RedundantParenthesesCheck.KEY)
public class RedundantParenthesesCheck extends PHPVisitorCheck {

  public static final String KEY = "S1110";
  private static final String MESSAGE = "Remove these useless parentheses.";

  @Override
  public void visitParenthesisedExpression(ParenthesisedExpressionTree tree) {
    ExpressionTree expression = tree.expression();
    if (expression.is(Tree.Kind.PARENTHESISED_EXPRESSION)) {
      ParenthesisedExpressionTree parentheses = (ParenthesisedExpressionTree) expression;
      context().newIssue(this, parentheses.openParenthesis(), MESSAGE).secondary(parentheses.closeParenthesis(), null);
    }
    super.visitParenthesisedExpression(tree);
  }

}
