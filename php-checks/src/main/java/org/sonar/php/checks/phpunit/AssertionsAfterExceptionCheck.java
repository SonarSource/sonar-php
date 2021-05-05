/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.php.utils.collections.SetUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.statement.ThrowStatementTree;

@Rule(key = "S5915")
public class AssertionsAfterExceptionCheck extends PhpUnitCheck {
  private static final String MESSAGE = "Don't perform an assertion here; An exception is expected to be raised before its execution.";
  private static final String MESSAGE_SINGLE = "Refactor this test; if this assertion's argument raises an exception, the assertion will never get executed.";

  private static final Set<String> EXPECT_METHODS = SetUtils.immutableSetOf(
    "expectexception",
    "expectexceptionmessage",
    "expectexceptionmessagematches",
    "exceptexceptioncode"
  );
  private static final Set<String> EXPECT_ANNOTATIONS = SetUtils.immutableSetOf(
    "expectedexception",
    "expectexceptionmessage",
    "expectedexceptionmessage",
    "expectedexceptionmessageregexp"
  );

  private FunctionCallTree expectExceptionCall;
  private FunctionCallTree lastFunctionCall;
  private boolean hasOtherFunctionCalls;
  private final Deque<FunctionCallTree> assertionsStack = new ArrayDeque<>();
  private Tree currentMethodBody;

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (!isTestCaseMethod(tree)) {
      return;
    }

    expectExceptionCall = null;
    lastFunctionCall = null;
    hasOtherFunctionCalls = false;
    currentMethodBody = tree.body();

    super.visitMethodDeclaration(tree);

    if ((expectExceptionCall != null || hasExpectAnnotation(tree))
      && lastFunctionCall != null && isAssertion(lastFunctionCall)) {
      newIssue(lastFunctionCall.callee(), hasOtherFunctionCalls ? MESSAGE : MESSAGE_SINGLE);
    }
  }

  private static boolean hasExpectAnnotation(MethodDeclarationTree tree) {
    for (String method : EXPECT_ANNOTATIONS) {
      if (CheckUtils.hasAnnotation(tree, method)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (!isPhpUnitTestMethod()) {
      return;
    }

    String functionName = CheckUtils.lowerCaseFunctionName(tree);
    if (EXPECT_METHODS.contains(functionName) && isMainStatementInBody(tree)) {
      expectExceptionCall = tree;
    } else if (isAssertion(tree)) {
      assertionsStack.add(tree);
    } else if (assertionsStack.isEmpty()) {
      hasOtherFunctionCalls = true;
    }

    super.visitFunctionCall(tree);
    lastFunctionCall = tree;

    if (isAssertion(tree)) {
      assertionsStack.pop();
    }
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    // Do not descend into anonymous functions
  }

  @Override
  public void visitThrowStatement(ThrowStatementTree tree) {
    lastFunctionCall = null;
  }

  private boolean isMainStatementInBody(FunctionCallTree tree) {
    Objects.requireNonNull(tree.getParent());
    return tree.getParent().getParent() == currentMethodBody;
  }
}
