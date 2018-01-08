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
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ConditionalExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = BooleanEqualityComparisonCheck.KEY)
public class BooleanEqualityComparisonCheck extends PHPVisitorCheck {

  public static final String KEY = "S1125";
  private static final String MESSAGE = "Remove the literal \"%s\" boolean value.";
  private static final Kind[] BINARY_CONDITIONAL_KINDS = {
    Kind.CONDITIONAL_OR,
    Kind.ALTERNATIVE_CONDITIONAL_OR,
    Kind.CONDITIONAL_AND,
    Kind.ALTERNATIVE_CONDITIONAL_AND,
    Kind.EQUAL_TO,
    Kind.NOT_EQUAL_TO,
    Kind.ALTERNATIVE_NOT_EQUAL_TO
  };

  @Override
  public void visitConditionalExpression(ConditionalExpressionTree tree) {
    check(tree.condition(), tree.falseExpression(), tree.trueExpression());
    super.visitConditionalExpression(tree);
  }

  @Override
  public void visitPrefixExpression(UnaryExpressionTree tree) {
    if (tree.is(Kind.LOGICAL_COMPLEMENT)) {
      check(tree.expression());
    }
    super.visitPrefixExpression(tree);
  }

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {
    if (tree.is(BINARY_CONDITIONAL_KINDS)) {
      check(tree.leftOperand(), tree.rightOperand());
    }
    super.visitBinaryExpression(tree);
  }

  private void check(ExpressionTree... expressions) {
    for (ExpressionTree expression : expressions) {
      if (expression != null && expression.is(Kind.BOOLEAN_LITERAL)) {
        context().newIssue(this, expression, String.format(MESSAGE, ((LiteralTree) expression).value()));
      }
    }
  }

}
