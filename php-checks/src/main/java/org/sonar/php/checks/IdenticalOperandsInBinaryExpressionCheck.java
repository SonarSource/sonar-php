/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.SyntacticEquivalence;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = IdenticalOperandsInBinaryExpressionCheck.KEY)
public class IdenticalOperandsInBinaryExpressionCheck extends PHPVisitorCheck {

  public static final String KEY = "S1764";

  private static final String MESSAGE = "Identical sub-expressions on both sides of operator \"%s\"";

  private static final Set<String> EXCLUDED_OPERATORS = Set.of("*", "+", ".", "**");

  @Override
  public void visitBinaryExpression(BinaryExpressionTree binaryExp) {
    String operator = binaryExp.operator().text();
    if (!EXCLUDED_OPERATORS.contains(operator) && hasIdenticalOperands(binaryExp) && !isLeftShiftBy1(binaryExp)) {
      context().newIssue(this, binaryExp.rightOperand(), String.format(MESSAGE, operator)).secondary(binaryExp.leftOperand(), null);
    }
    super.visitBinaryExpression(binaryExp);
  }

  private static boolean hasIdenticalOperands(BinaryExpressionTree binaryExp) {
    return SyntacticEquivalence.areSyntacticallyEquivalent(binaryExp.leftOperand(), binaryExp.rightOperand());
  }

  private static boolean isLeftShiftBy1(BinaryExpressionTree binaryExp) {
    if (binaryExp.is(Kind.LEFT_SHIFT) && binaryExp.rightOperand().is(Kind.NUMERIC_LITERAL)) {
      LiteralTree rightOperand = (LiteralTree) binaryExp.rightOperand();
      return "1".equals(rightOperand.token().text());
    }
    return false;
  }

}
