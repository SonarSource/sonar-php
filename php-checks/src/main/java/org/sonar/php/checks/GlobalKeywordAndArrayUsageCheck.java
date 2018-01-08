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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ArrayAccessTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.GlobalStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = GlobalKeywordAndArrayUsageCheck.KEY)
public class GlobalKeywordAndArrayUsageCheck extends PHPVisitorCheck {

  public static final String KEY = "S2011";
  private static final String MESSAGE = "Pass this global variable to the function as a parameter rather than accessing it directly.";

  private static final String GLOBAL_IDENTIFIER = "$GLOBALS";

  @Override
  public void visitGlobalStatement(GlobalStatementTree tree) {
    super.visitGlobalStatement(tree);
    raiseIssue(tree);
  }

  @Override
  public void visitArrayAccess(ArrayAccessTree tree) {
    super.visitArrayAccess(tree);

    ExpressionTree object = tree.object();

    if (object.is(Kind.VARIABLE_IDENTIFIER)) {
      String name = ((VariableIdentifierTree) object).variableExpression().text();

      if (name.equals(GLOBAL_IDENTIFIER)) {
        raiseIssue(tree);
      }
    }
  }

  private void raiseIssue(Tree tree) {
    context().newIssue(this, tree, MESSAGE);
  }

}
