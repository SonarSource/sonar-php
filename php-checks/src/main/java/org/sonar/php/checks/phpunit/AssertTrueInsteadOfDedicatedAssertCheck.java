/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.checks.phpunit;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.utils.collections.MapBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;

import static org.sonar.php.checks.utils.CheckUtils.skipParenthesis;
import static org.sonar.plugins.php.api.tree.Tree.Kind.BOOLEAN_LITERAL;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NULL_LITERAL;

@Rule(key = "S5785")
public class AssertTrueInsteadOfDedicatedAssertCheck extends PhpUnitCheck {

  private static final String MESSAGE_USE_INSTEAD = "Use %s instead.";
  private static final String SECONDARY_MESSAGE_USE_INSTEAD = "%s is performed here, which is better expressed with %s.";
  private static final String MESSAGE_SIMPLIFY = "Simplify this expression by removing the %s comparison to %s%s.";
  private static final String ASSERT_TRUE = "assertTrue";
  private static final String ASSERT_FALSE = "assertFalse";
  private static final Set<String> ASSERT_BOOLEAN_METHOD_NAMES = Set.of(ASSERT_TRUE, ASSERT_FALSE);

  private static final Map<ReplacementAssertion, ReplacementAssertion> COMPLEMENTS = MapBuilder.<ReplacementAssertion, ReplacementAssertion>builder()
    .put(ReplacementAssertion.NULL, ReplacementAssertion.NOT_NULL)
    .put(ReplacementAssertion.NOT_NULL, ReplacementAssertion.NULL)
    .put(ReplacementAssertion.SAME, ReplacementAssertion.NOT_SAME)
    .put(ReplacementAssertion.NOT_SAME, ReplacementAssertion.SAME)
    .put(ReplacementAssertion.EQUALS, ReplacementAssertion.NOT_EQUALS)
    .put(ReplacementAssertion.NOT_EQUALS, ReplacementAssertion.EQUALS)
    .build();
  private static final Map<Tree.Kind, String> COMPARISON_TO_MESSAGE = Map.of(
    Tree.Kind.EQUAL_TO, "equality",
    Tree.Kind.STRICT_EQUAL_TO, "equality",
    Tree.Kind.NOT_EQUAL_TO, "non-equality",
    Tree.Kind.STRICT_NOT_EQUAL_TO, "non-equality");

  private enum ReplacementAssertion {
    NULL("Null", "A null-check"),
    NOT_NULL("NotNull", "A null-check"),
    SAME("Same", "A type-safe equality check"),
    NOT_SAME("NotSame", "A type-safe equality check"),
    EQUALS("Equals", "An equality check"),
    NOT_EQUALS("NotEquals", "An equality check");

    public final String methodName;
    public final String actionDescription;
    public final String useInsteadMessage;
    public final String secondaryExplanationMessage;

    ReplacementAssertion(String namePostfix, String actionDescription) {
      this.methodName = "assert" + namePostfix;
      this.actionDescription = actionDescription;
      this.useInsteadMessage = String.format(MESSAGE_USE_INSTEAD, methodName);
      this.secondaryExplanationMessage = String.format(SECONDARY_MESSAGE_USE_INSTEAD, actionDescription, methodName);
    }
  }

  @Override
  public void visitFunctionCall(FunctionCallTree fct) {
    if (!isPhpUnitTestMethod()) {
      return;
    }

    getAssertion(fct).ifPresent(a -> {
      if (ASSERT_BOOLEAN_METHOD_NAMES.contains(a.name())) {
        checkBooleanExpressionInAssertMethod(fct, a.name());
      }
    });

    super.visitFunctionCall(fct);
  }

  private void checkBooleanExpressionInAssertMethod(FunctionCallTree problematicAssertionCall, String assertionName) {
    CheckUtils.argumentValue(problematicAssertionCall, "", 0).ifPresent(argumentExpression -> booleanComparisonWithBooleanLiteral(argumentExpression)
      .ifPresentOrElse(booleanLiteralToRemove -> {
        // Boolean comparison, raise an issue to simplify it
        BinaryExpressionTree bet = (BinaryExpressionTree) argumentExpression;
        String comparisonToRemove = COMPARISON_TO_MESSAGE.get(bet.getKind());
        String additionalAction = computeAdditionalAction(assertionName, booleanLiteralToRemove, bet);
        String message = String.format(MESSAGE_SIMPLIFY, comparisonToRemove, booleanLiteralToRemove, additionalAction);
        newIssue(problematicAssertionCall, message);
      }, () -> {
        // Non-boolean comparison, raise an issue to replace it
        Optional<ReplacementAssertion> replacementAssertionOpt = getReplacementAssertion(argumentExpression);
        if (assertionName.equals(ASSERT_FALSE)) {
          replacementAssertionOpt = replacementAssertionOpt.map(COMPLEMENTS::get);
        }
        replacementAssertionOpt.ifPresent(replacementAssertion -> reportIssue(problematicAssertionCall, replacementAssertion, argumentExpression));
      }));
  }

  private static String computeAdditionalAction(String assertionName, String booleanLiteralValue, BinaryExpressionTree bet) {
    boolean isAssertTrue = ASSERT_TRUE.equals(assertionName);
    boolean isTrueValue = "true".equals(booleanLiteralValue);
    boolean isEqualComparison = bet.is(Tree.Kind.EQUAL_TO, Tree.Kind.STRICT_EQUAL_TO);

    if ((isAssertTrue && isTrueValue == isEqualComparison) || (!isAssertTrue && isTrueValue)) {
      return "";
    } else {
      return String.format(" and use %s instead", isAssertTrue ? ASSERT_FALSE : ASSERT_TRUE);
    }
  }

  private void reportIssue(FunctionCallTree problematicAssertionCall, ReplacementAssertion replacementAssertion, ExpressionTree argumentExpression) {
    newIssue(problematicAssertionCall, replacementAssertion.useInsteadMessage)
      .secondary(argumentExpression, replacementAssertion.secondaryExplanationMessage);
  }

  /**
   * Returns the assertX method that should be used instead of assertTrue, if applicable.
   *
   * @param argumentExpression the boolean expression passed to assertTrue
   * @return the assertion method to be used instead of assertTrue, or {@code null} if no better assertion method was determined
   */
  private static Optional<ReplacementAssertion> getReplacementAssertion(ExpressionTree argumentExpression) {
    ReplacementAssertion replacementAssertion = null;
    argumentExpression = skipParenthesis(argumentExpression);

    switch (argumentExpression.getKind()) {
      case EQUAL_TO:
        if (isCheckForNull((BinaryExpressionTree) argumentExpression)) {
          replacementAssertion = ReplacementAssertion.NULL;
        } else {
          replacementAssertion = ReplacementAssertion.EQUALS;
        }
        break;
      case STRICT_EQUAL_TO:
        if (isCheckForNull((BinaryExpressionTree) argumentExpression)) {
          replacementAssertion = ReplacementAssertion.NULL;
        } else {
          replacementAssertion = ReplacementAssertion.SAME;
        }
        break;
      case NOT_EQUAL_TO:
        if (isCheckForNull((BinaryExpressionTree) argumentExpression)) {
          replacementAssertion = ReplacementAssertion.NOT_NULL;
        } else {
          replacementAssertion = ReplacementAssertion.NOT_EQUALS;
        }
        break;
      case STRICT_NOT_EQUAL_TO:
        if (isCheckForNull((BinaryExpressionTree) argumentExpression)) {
          replacementAssertion = ReplacementAssertion.NOT_NULL;
        } else {
          replacementAssertion = ReplacementAssertion.NOT_SAME;
        }
        break;
      case LOGICAL_COMPLEMENT:
        return getReplacementAssertion(((UnaryExpressionTree) argumentExpression).expression()).map(COMPLEMENTS::get);
      default:
    }

    return Optional.ofNullable(replacementAssertion);
  }

  private static boolean isCheckForNull(BinaryExpressionTree bet) {
    return bet.leftOperand().is(NULL_LITERAL) || bet.rightOperand().is(NULL_LITERAL);
  }

  private static Optional<String> booleanComparisonWithBooleanLiteral(ExpressionTree expr) {
    if (expr.is(Tree.Kind.EQUAL_TO, Tree.Kind.STRICT_EQUAL_TO, Tree.Kind.NOT_EQUAL_TO, Tree.Kind.STRICT_NOT_EQUAL_TO)) {
      BinaryExpressionTree bet = (BinaryExpressionTree) expr;
      if (bet.leftOperand().is(BOOLEAN_LITERAL)) {
        return Optional.of(((LiteralTree) bet.leftOperand()).value());
      } else if (bet.rightOperand().is(BOOLEAN_LITERAL)) {
        return Optional.of(((LiteralTree) bet.rightOperand()).value());
      }
    }
    return Optional.empty();
  }
}
