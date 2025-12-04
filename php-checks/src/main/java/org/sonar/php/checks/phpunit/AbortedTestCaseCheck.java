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
package org.sonar.php.checks.phpunit;

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

import static org.sonar.php.checks.utils.CheckUtils.lowerCaseFunctionName;

@Rule(key = "S1607")
public class AbortedTestCaseCheck extends PhpUnitCheck {

  private static final String MESSAGE = "Either remove this call or add an explanation about why the test is aborted.";
  private static final Set<String> ABORT_FUNCTIONS = Set.of("marktestskipped", "marktestincomplete");

  @Override
  public void visitFunctionCall(FunctionCallTree fct) {
    if (!isPhpUnitTestCase()) {
      return;
    }

    if (isAbortFunctionWithoutMessage(fct)) {
      newIssue(fct, MESSAGE);
    }

    super.visitFunctionCall(fct);
  }

  private static boolean isAbortFunctionWithoutMessage(FunctionCallTree fct) {
    String name = lowerCaseFunctionName(fct);
    return name != null && ABORT_FUNCTIONS.contains(name) && fct.callArguments().isEmpty();
  }
}
