/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = IncrementDecrementInSubExpressionCheck.KEY)
public class IncrementDecrementInSubExpressionCheck extends PHPVisitorCheck {

  public static final String KEY = "S881";
  private static final String MESSAGE = "Extract this increment or decrement operator into a dedicated statement.";

  private static final Kind[] INC_DEC = {
    Kind.PREFIX_DECREMENT,
    Kind.PREFIX_INCREMENT,
    Kind.POSTFIX_DECREMENT,
    Kind.POSTFIX_INCREMENT};

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
    if (!tree.expression().is(INC_DEC)) {
      super.visitExpressionStatement(tree);
    }
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    scanExpressionList(tree.init());
    scanExpressionList(tree.condition());
    scanExpressionList(tree.update());

    scan(tree.statements());
  }

  private void scanExpressionList(SeparatedList<ExpressionTree> list) {
    for (ExpressionTree expression : list) {
      if (expression.is(INC_DEC)) {
        scan(((UnaryExpressionTree) expression).expression());
      } else {
        scan(expression);
      }
    }
  }

  @Override
  public void visitPostfixExpression(UnaryExpressionTree tree) {
    if (tree.is(INC_DEC)) {
      raiseIssue(tree);
    }
    super.visitPostfixExpression(tree);
  }

  @Override
  public void visitPrefixExpression(UnaryExpressionTree tree) {
    if (tree.is(INC_DEC)) {
      raiseIssue(tree);
    }
    super.visitPrefixExpression(tree);
  }

  private void raiseIssue(Tree tree) {
    context().newIssue(this, tree, MESSAGE);
  }
}
