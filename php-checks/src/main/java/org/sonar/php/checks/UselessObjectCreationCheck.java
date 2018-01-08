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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = UselessObjectCreationCheck.KEY)
public class UselessObjectCreationCheck extends PHPVisitorCheck {

  public static final String KEY = "S1848";
  private static final String MESSAGE = "Either remove this useless object instantiation of class \"%s\" or use it";

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
    super.visitExpressionStatement(tree);

    if (tree.expression().is(Tree.Kind.NEW_EXPRESSION)) {
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

}
