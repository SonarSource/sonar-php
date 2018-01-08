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
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.NewExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S3984")
public class UnusedExceptionCheck extends PHPVisitorCheck {

  @Override
  public void visitExpressionStatement(ExpressionStatementTree tree) {
    if (tree.expression().is(Tree.Kind.NEW_EXPRESSION)) {
      NewExpressionTree newExpressionTree = (NewExpressionTree) tree.expression();
      ExpressionTree checkForException = newExpressionTree.expression();
      if (checkForException.is(Tree.Kind.FUNCTION_CALL)) {
        checkForException = ((FunctionCallTree) checkForException).callee();
      }
      if (((PHPTree) checkForException).getLastToken().text().endsWith("Exception")) {
        context().newIssue(this, newExpressionTree, "Throw this exception or remove this useless statement");
      }
    }
    super.visitExpressionStatement(tree);
  }
}
