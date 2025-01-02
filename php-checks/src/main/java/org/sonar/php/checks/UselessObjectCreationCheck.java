/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1848")
public class UselessObjectCreationCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Either remove this useless object instantiation of class \"%s\" or use it";

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
    super.visitExpressionStatement(tree);

    if (tree.expression().is(Tree.Kind.NEW_EXPRESSION)) {
      if (isSingleStatementInTryCatch(tree)) {
        return;
      }
      NewExpressionTree newExpression = (NewExpressionTree) tree.expression();
      String message = String.format(MESSAGE, getClassName(newExpression.expression()));
      context().newIssue(this, newExpression, message);
    }
  }

  private static String getClassName(ExpressionTree expression) {
    if (expression.is(Tree.Kind.FUNCTION_CALL)) {
      return ((FunctionCallTree) expression).callee().toString();

    } else {
      return expression.toString();
    }
  }

  private static boolean isSingleStatementInTryCatch(ExpressionStatementTree tree) {
    if (tree.getParent().is(Tree.Kind.BLOCK)) {
      BlockTree blocks = (BlockTree) tree.getParent();
      return tree.getParent().getParent().is(Tree.Kind.TRY_STATEMENT) && blocks.statements().size() == 1;
    }
    return false;
  }
}
