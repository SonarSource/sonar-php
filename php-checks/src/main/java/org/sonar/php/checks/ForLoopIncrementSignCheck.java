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

import java.util.List;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.SyntacticEquivalence;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ForStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2251")
public class ForLoopIncrementSignCheck extends PHPVisitorCheck {

  private enum UpdateKind {
    PLUS, MINUS, UNKNOWN
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    List<ExpressionTree> updates = tree.update().stream()
      .filter(expression -> updateKind(expression) != UpdateKind.UNKNOWN)
      .collect(Collectors.toList());

    ExpressionTree forCondition = CheckUtils.getForCondition(tree);
    if (forCondition != null && !updates.isEmpty()) {
      checkCondition(forCondition, updates);
    }
    super.visitForStatement(tree);
  }

  private void checkCondition(ExpressionTree tree, List<ExpressionTree> updates) {
    ExpressionTree condition = CheckUtils.skipParenthesis(tree);
    if (condition.is(Kind.CONDITIONAL_OR)) {
      BinaryExpressionTree conditionalOr = (BinaryExpressionTree) condition;
      checkCondition(conditionalOr.leftOperand(), updates);
      checkCondition(conditionalOr.rightOperand(), updates);
    } else if (condition.is(Kind.LESS_THAN, Kind.LESS_THAN_OR_EQUAL_TO, Kind.GREATER_THAN, Kind.GREATER_THAN_OR_EQUAL_TO)) {
      BinaryExpressionTree comparison = (BinaryExpressionTree) condition;
      boolean isLessThan = condition.is(Kind.LESS_THAN, Kind.LESS_THAN_OR_EQUAL_TO);

      updates.stream()
        .filter(update -> updateKind(update) == UpdateKind.PLUS)
        .forEach(update -> checkOperand(update, comparison, isLessThan ? comparison.rightOperand() : comparison.leftOperand()));

      updates.stream()
        .filter(update -> updateKind(update) == UpdateKind.MINUS)
        .forEach(update -> checkOperand(update, comparison, isLessThan ? comparison.leftOperand() : comparison.rightOperand()));
    }
  }

  private void checkOperand(ExpressionTree update, BinaryExpressionTree binaryExpression, ExpressionTree operandToCheck) {
    ExpressionTree operand = CheckUtils.skipParenthesis(operandToCheck);
    if (SyntacticEquivalence.areSyntacticallyEquivalent(variable(update), operand)) {
      String type = updateKind(update) == UpdateKind.PLUS ? "incremented" : "decremented";
      context().newIssue(this, update, "\"" + operand.toString() + "\" is " + type + " and will never reach \"stop condition\".")
        .secondary(binaryExpression, "Stop condition");
    }
  }

  private static ExpressionTree variable(ExpressionTree expression) {
    if (expression.is(Kind.PLUS_ASSIGNMENT, Kind.MINUS_ASSIGNMENT)) {
      return ((AssignmentExpressionTree) expression).variable();
    }
    return ((UnaryExpressionTree) expression).expression();
  }

  private static UpdateKind updateKind(ExpressionTree expression) {
    if (expression.is(Kind.PREFIX_INCREMENT, Kind.POSTFIX_INCREMENT)) {
      return UpdateKind.PLUS;
    } else if (expression.is(Kind.PREFIX_DECREMENT, Kind.POSTFIX_DECREMENT)) {
      return UpdateKind.MINUS;
    } else if (expression.is(Kind.PLUS_ASSIGNMENT)) {
      return valueKind(((AssignmentExpressionTree) expression).value());
    } else if (expression.is(Kind.MINUS_ASSIGNMENT)) {
      return minus(valueKind(((AssignmentExpressionTree) expression).value()));
    }
    return UpdateKind.UNKNOWN;
  }

  private static UpdateKind valueKind(ExpressionTree value) {
    if (value.is(Kind.NUMERIC_LITERAL)) {
      return UpdateKind.PLUS;
    } else if (value.is(Kind.UNARY_MINUS)) {
      return minus(valueKind(((UnaryExpressionTree) value).expression()));
    } else if (value.is(Kind.UNARY_PLUS)) {
      return valueKind(((UnaryExpressionTree) value).expression());
    }
    return UpdateKind.UNKNOWN;
  }

  private static UpdateKind minus(UpdateKind kind) {
    if (kind == UpdateKind.PLUS) {
      return UpdateKind.MINUS;
    } else if (kind == UpdateKind.MINUS) {
      return UpdateKind.PLUS;
    } else {
      return UpdateKind.UNKNOWN;
    }
  }
}
