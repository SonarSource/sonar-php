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
package org.sonar.php.checks;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.YieldExpressionTree;
import org.sonar.plugins.php.api.tree.statement.BreakStatementTree;
import org.sonar.plugins.php.api.tree.statement.CaseClauseTree;
import org.sonar.plugins.php.api.tree.statement.ContinueStatementTree;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S6600")
public class ConstructWithParenthesesCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Remove the parentheses from this \"%s\" call.";
  private static final Set<String> CONSTRUCT_FUNCTION = Set.of("echo", "clone", "include", "include_once", "require", "require_once", "print");

  @Override
  public void visitCaseClause(CaseClauseTree tree) {
    raiseIssueOnParenthesizedExpression(tree.expression(), tree.caseToken(), tree.caseSeparatorToken(), "case");
    super.visitCaseClause(tree);
  }

  @Override
  public void visitBreakStatement(BreakStatementTree tree) {
    raiseIssueOnParenthesizedExpression(tree.argument(), tree, "break");
    super.visitBreakStatement(tree);
  }

  @Override
  public void visitReturnStatement(ReturnStatementTree tree) {
    raiseIssueOnParenthesizedExpression(tree.expression(), tree, "return");
    super.visitReturnStatement(tree);
  }

  @Override
  public void visitThrowStatement(ThrowStatementTree tree) {
    raiseIssueOnParenthesizedExpression(tree.expression(), tree, "throw");
    super.visitThrowStatement(tree);
  }

  @Override
  public void visitYieldExpression(YieldExpressionTree tree) {
    String constructName = tree.fromToken() != null ? "yield from" : "yield";
    ExpressionTree elementToCheck = tree.key() != null ? tree.key() : tree.value();
    raiseIssueOnParenthesizedExpression(elementToCheck, tree, constructName);
    super.visitYieldExpression(tree);
  }

  @Override
  public void visitContinueStatement(ContinueStatementTree tree) {
    raiseIssueOnParenthesizedExpression(tree.argument(), tree, "continue");
    super.visitContinueStatement(tree);
  }

  private void raiseIssueOnParenthesizedExpression(@Nullable ExpressionTree expression, Tree raiseIssueOn, String constructName) {
    if (isParenthesizedExpression(expression)) {
      newIssue(raiseIssueOn, String.format(MESSAGE, constructName));
    }
  }

  private void raiseIssueOnParenthesizedExpression(@Nullable ExpressionTree expression, Tree raiseIssueOnStart, Tree raiseIssueOnEnd, String constructName) {
    if (isParenthesizedExpression(expression)) {
      context().newIssue(this, raiseIssueOnStart, raiseIssueOnEnd, String.format(MESSAGE, constructName));
    }
  }

  private static boolean isParenthesizedExpression(@Nullable ExpressionTree expression) {
    return expression != null && expression.is(Tree.Kind.PARENTHESISED_EXPRESSION);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String calleeName = getCalleeName(tree.callee());
    if (isConstructFunction(calleeName) && hasParenthesizedArgument(tree)) {
      newIssue(tree, String.format(MESSAGE, calleeName));
    }
    super.visitFunctionCall(tree);
  }

  private static boolean hasParenthesizedArgument(FunctionCallTree tree) {
    return tree.callArguments().size() == 1 && isParenthesized(tree.callArguments().get(0).value());
  }

  private static boolean isParenthesized(ExpressionTree argument) {
    return argument.is(Tree.Kind.PARENTHESISED_EXPRESSION)
      || (argument instanceof BinaryExpressionTree && !isExcludedBinaryExpression((BinaryExpressionTree) argument)
      && isParenthesized(((BinaryExpressionTree) argument).leftOperand()));
  }

  private static boolean isExcludedBinaryExpression(BinaryExpressionTree binaryExpression) {
    return binaryExpression.is(
      Tree.Kind.CONCATENATION, Tree.Kind.POWER, Tree.Kind.MULTIPLY, Tree.Kind.DIVIDE, Tree.Kind.REMAINDER, Tree.Kind.PLUS, Tree.Kind.MINUS,
      Tree.Kind.LEFT_SHIFT, Tree.Kind.RIGHT_SHIFT);
  }

  private static boolean isConstructFunction(@Nullable String constructName) {
    return constructName != null && CONSTRUCT_FUNCTION.contains(constructName);
  }

  @CheckForNull
  private static String getCalleeName(ExpressionTree callee) {
    return Optional.of(callee)
      .filter(cal -> cal.is(Tree.Kind.NAMESPACE_NAME))
      .map(cal -> ((NamespaceNameTree) cal).qualifiedName().toLowerCase(Locale.ROOT))
      .orElse(null);
  }

}
