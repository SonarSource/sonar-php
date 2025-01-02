/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.getLowerCaseFunctionName;

@Rule(key = "S2255")
public class CookieSensitiveDataCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure that this cookie is written safely.";

  private static final List<String> SET_COOKIE_FUNCTIONS = Arrays.asList("setcookie", "setrawcookie");
  private static final int VALUE_PARAMETER_INDEX = 1;
  private static final String VALUE_PARAMETER_NAME = "value";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = getLowerCaseFunctionName(tree);
    if (SET_COOKIE_FUNCTIONS.contains(functionName) && hasCookieValue(tree)) {
      context().newIssue(this, tree.callee(), MESSAGE);
    }

    super.visitFunctionCall(tree);
  }

  private static boolean hasCookieValue(FunctionCallTree tree) {
    Optional<CallArgumentTree> argument = CheckUtils.argument(tree, VALUE_PARAMETER_NAME, VALUE_PARAMETER_INDEX);

    return argument.isPresent() && !CheckUtils.isNullOrEmptyString(argument.get().value());
  }

}
