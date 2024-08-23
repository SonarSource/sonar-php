/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
import org.sonar.plugins.php.api.tree.statement.IfStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ConstantConditionCheck.KEY)
public class ConstantConditionCheck extends PHPVisitorCheck {

  public static final String KEY = "S5797";
  private static final String MESSAGE = "Replace this expression; used as a condition it will always be constant.";
  private static final Tree.Kind[] LITERAL_KINDS = {
    Tree.Kind.BOOLEAN_LITERAL,
    Tree.Kind.NUMERIC_LITERAL,
    Tree.Kind.REGULAR_STRING_LITERAL,
    Tree.Kind.NULL_LITERAL,
    Tree.Kind.ARRAY_INITIALIZER_FUNCTION,
  };

  @Override
  public void visitIfStatement(IfStatementTree tree) {
    ExpressionTree conditionExpression = tree.condition().expression();
    if (conditionExpression.is(LITERAL_KINDS)) {
      newIssue(conditionExpression, MESSAGE);
    }
    super.visitIfStatement(tree);
  }
}
