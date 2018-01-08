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

import java.util.Arrays;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;

@Rule(key = ArrayCountableCountCheck.KEY)
public class ArrayCountableCountCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S3981";

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Arrays.asList(Tree.Kind.LESS_THAN, Tree.Kind.LESS_THAN_OR_EQUAL_TO, Tree.Kind.GREATER_THAN, Tree.Kind.GREATER_THAN_OR_EQUAL_TO);
  }

  @Override
  public void visitNode(Tree tree) {
    BinaryExpressionTree bet = (BinaryExpressionTree) tree;

    ExpressionTree leftOperand = CheckUtils.skipParenthesis(bet.leftOperand());
    ExpressionTree rightOperand = CheckUtils.skipParenthesis(bet.rightOperand());
    boolean leftIsZero = isZero(leftOperand);
    boolean rightIsZero = isZero(rightOperand);

    if (!leftIsZero && !rightIsZero) {
      return;
    }

    ExpressionTree testedValue = leftIsZero ? rightOperand : leftOperand;
    if (!isCountCall(testedValue)) {
      return;
    }

    if ((leftIsZero && bet.is(Tree.Kind.GREATER_THAN, Tree.Kind.LESS_THAN_OR_EQUAL_TO))
      || (!leftIsZero && bet.is(Tree.Kind.LESS_THAN, Tree.Kind.GREATER_THAN_OR_EQUAL_TO))) {
      context().newIssue(this, bet, "The count of an array or Countable is always \">=0\", so update this test to either \"==0\" or \">0\".");
    }
  }

  private static boolean isZero(ExpressionTree expr) {
    return expr.is(Tree.Kind.NUMERIC_LITERAL) && "0".equals(((LiteralTree) expr).value());
  }

  private static boolean isCountCall(ExpressionTree testedValue) {
    return testedValue.is(Tree.Kind.FUNCTION_CALL) && "count".equalsIgnoreCase(((FunctionCallTree) testedValue).callee().toString());
  }
}
