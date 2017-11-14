/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import java.util.List;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.SyntacticEquivalence;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2251")
public class ForLoopIncrementSignCheck extends PHPVisitorCheck {

  @Override
  public void visitForStatement(ForStatementTree tree) {
    List<UnaryExpressionTree> unaryOperations = tree.update().stream()
      .filter(expression -> expression.is(Kind.PREFIX_INCREMENT, Kind.PREFIX_DECREMENT, Kind.POSTFIX_INCREMENT, Kind.POSTFIX_DECREMENT))
      .map(UnaryExpressionTree.class::cast)
      .collect(Collectors.toList());

    if (!unaryOperations.isEmpty() && !tree.condition().isEmpty()) {
      checkCondition(tree.condition().get(tree.condition().size() - 1), unaryOperations);
    }
    super.visitForStatement(tree);
  }

  private void checkCondition(ExpressionTree tree, List<UnaryExpressionTree> unaryOperations) {
    ExpressionTree condition = CheckUtils.skipParenthesis(tree);
    if (condition.is(Kind.CONDITIONAL_OR)) {
      BinaryExpressionTree conditionalOr = (BinaryExpressionTree) condition;
      checkCondition(conditionalOr.leftOperand(), unaryOperations);
      checkCondition(conditionalOr.rightOperand(), unaryOperations);
    } else if (condition.is(Kind.LESS_THAN, Kind.LESS_THAN_OR_EQUAL_TO, Kind.GREATER_THAN, Kind.GREATER_THAN_OR_EQUAL_TO)) {
      BinaryExpressionTree comparison = (BinaryExpressionTree) condition;
      boolean isLessThan = condition.is(Kind.LESS_THAN, Kind.LESS_THAN_OR_EQUAL_TO);

      unaryOperations.stream()
        .filter(operation -> operation.is(Kind.PREFIX_INCREMENT, Kind.POSTFIX_INCREMENT))
        .forEach(operation -> checkOperand(operation, comparison, isLessThan ? comparison.rightOperand() : comparison.leftOperand()));

      unaryOperations.stream()
        .filter(operation -> operation.is(Kind.PREFIX_DECREMENT, Kind.POSTFIX_DECREMENT))
        .forEach(operation -> checkOperand(operation, comparison, isLessThan ? comparison.leftOperand() : comparison.rightOperand()));
    }
  }

  private void checkOperand(UnaryExpressionTree operation, BinaryExpressionTree binaryExpression, ExpressionTree operandToCheck) {
    ExpressionTree operand = CheckUtils.skipParenthesis(operandToCheck);
    if (SyntacticEquivalence.areSyntacticallyEquivalent(operation.expression(), operand)) {
      String type = operation.is(Kind.PREFIX_INCREMENT, Kind.POSTFIX_INCREMENT) ? "incremented" : "decremented";
      context().newIssue(this, operation, "\"" + operand.toString() + "\" is " + type + " and will never reach \"stop condition\".")
        .secondary(binaryExpression, "Stop condition");
    }
  }
}
