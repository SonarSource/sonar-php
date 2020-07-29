/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;

import static org.sonar.php.checks.utils.CheckUtils.skipParenthesis;
import static org.sonar.plugins.php.api.tree.Tree.Kind.NULL_LITERAL;

@Rule(key = "S5785")
public class AssertTrueInsteadOfDedicatedAssertCheck extends PhpUnitCheck {

  private static final String MESSAGE = "Use %s instead.";
  private static final String SECONDARY_MESSAGE = "%s is performed here, which is better expressed with %s.";
  private static final ImmutableSet<String> ASSERT_METHOD_NAMES = ImmutableSet.of("assertTrue", "assertFalse");

  private static final Map<ReplacementAssertion, ReplacementAssertion> COMPLEMENTS = ImmutableMap.<ReplacementAssertion, ReplacementAssertion>builder()
    .put(ReplacementAssertion.NULL, ReplacementAssertion.NOT_NULL)
    .put(ReplacementAssertion.NOT_NULL, ReplacementAssertion.NULL)
    .put(ReplacementAssertion.SAME, ReplacementAssertion.NOT_SAME)
    .put(ReplacementAssertion.NOT_SAME, ReplacementAssertion.SAME)
    .put(ReplacementAssertion.EQUALS, ReplacementAssertion.NOT_EQUALS)
    .put(ReplacementAssertion.NOT_EQUALS, ReplacementAssertion.EQUALS)
    .build();

  private enum ReplacementAssertion {
    NULL("Null", "A null-check"),
    NOT_NULL("NotNull", "A null-check"),
    SAME("Same", "An object reference comparison"),
    NOT_SAME("NotSame", "An object reference comparison"),
    EQUALS("Equals", "An equals check"),
    NOT_EQUALS("NotEquals", "An equals check");

    public final String methodName;
    public final String actionDescription;
    public final String useInsteadMessage;
    public final String secondaryExplanationMessage;

    ReplacementAssertion(String namePostfix, String actionDescription) {
      this.methodName = "assert" + namePostfix;
      this.actionDescription = actionDescription;
      this.useInsteadMessage = String.format(MESSAGE, methodName);
      this.secondaryExplanationMessage = String.format(SECONDARY_MESSAGE, actionDescription, methodName);
    }
  }

  @Override
  public void visitFunctionCall(FunctionCallTree fct) {
    if (isPhpUnitTestMethod()) {
      Optional<Assertion> assertion = getAssertion(fct);
      assertion.ifPresent(a -> { if (ASSERT_METHOD_NAMES.contains(a.name())) { checkBooleanExpressionInAssertMethod(fct, a.name()); }});
    }
    super.visitFunctionCall(fct);
  }

  private void checkBooleanExpressionInAssertMethod(FunctionCallTree problematicAssertionCall, String assertionName) {
    ExpressionTree argumentExpression = problematicAssertionCall.arguments().get(0);
    Optional<ReplacementAssertion> replacementAssertionOpt = getReplacementAssertion(argumentExpression);
    if (assertionName.equals("assertFalse")) {
      replacementAssertionOpt = replacementAssertionOpt.map(COMPLEMENTS::get);
    }

    replacementAssertionOpt.ifPresent(replacementAssertion -> reportIssue(problematicAssertionCall, replacementAssertion, argumentExpression));
  }

  private void reportIssue(FunctionCallTree problematicAssertionCall, ReplacementAssertion replacementAssertion, ExpressionTree argumentExpression) {
    context().newIssue(this, problematicAssertionCall, replacementAssertion.useInsteadMessage)
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
        if (isPositionVerification((BinaryExpressionTree) argumentExpression)) {
          break;
        } else if (isCheckForNull((BinaryExpressionTree) argumentExpression)) {
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
        if (isPositionVerification((BinaryExpressionTree) argumentExpression)) {
          break;
        } else if (isCheckForNull((BinaryExpressionTree) argumentExpression)) {
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

  private static boolean isPositionVerification(BinaryExpressionTree bet) {
    return isBooleanFalse(bet.leftOperand()) ^ isBooleanFalse(bet.rightOperand());
  }

  /**
   * There are built-in methods which return integer or false (e.g. for example strpos, see https://www.php.net/manual/en/function.strpos.php)
   * If you want to check if the boolean value is returned, you can't do this with assertFalse, because a typecast of integer 0 would also apply here.
   * So if a strict comparison with boolean should compare false, this rule should not be applied.
   */
  private static boolean isBooleanFalse(ExpressionTree tree) {
    return tree.is(Tree.Kind.BOOLEAN_LITERAL) && ((LiteralTree) tree).value().equalsIgnoreCase("false");
  }
}
