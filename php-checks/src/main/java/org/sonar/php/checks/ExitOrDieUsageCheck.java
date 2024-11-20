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
package org.sonar.php.checks;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = ExitOrDieUsageCheck.KEY)
public class ExitOrDieUsageCheck extends PHPVisitorCheck {

  public static final String KEY = "S1799";
  public static final String MESSAGE = "Remove this \"%s()\" call or ensure it is really required";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    if (CheckUtils.isExitExpression(tree)) {
      String callee = tree.callee().toString();
      context().newIssue(this, tree.callee(), String.format(MESSAGE, callee));
    }

    super.visitFunctionCall(tree);
  }

}
