/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.checks.security;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S5328")
public class SessionFixationCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure the session ID being set is cryptographically secure and is not user-supplied.";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (isFunctionCall(tree, "session_id") && hasArguments(tree) && !isFunctionCall(firstCallArgument(tree), "session_create_id")) {
      context().newIssue(this, tree, MESSAGE);
    }
    super.visitFunctionCall(tree);
  }

  private static boolean isFunctionCall(ExpressionTree expression, String expectedName) {
    return expression.is(Kind.FUNCTION_CALL) && expectedName.equals(CheckUtils.getLowerCaseFunctionName((FunctionCallTree) expression));
  }

  private static ExpressionTree firstCallArgument(FunctionCallTree call) {
    return call.callArguments().get(0).value();
  }

  private boolean hasArguments(FunctionCallTree tree) {
    return !tree.callArguments().isEmpty();
  }
}
