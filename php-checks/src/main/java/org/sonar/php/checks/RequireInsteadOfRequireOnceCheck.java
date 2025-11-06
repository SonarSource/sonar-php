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
package org.sonar.php.checks;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = RequireInsteadOfRequireOnceCheck.KEY)
public class RequireInsteadOfRequireOnceCheck extends PHPVisitorCheck {

  public static final String KEY = "S2003";
  private static final String MESSAGE = "Replace \"%s\" with \"%s\".";

  private static final List<String> WRONG_FUNCTIONS = Arrays.asList("require", "include");

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    super.visitFunctionCall(tree);

    String callee = tree.callee().toString();
    if (!isLaravelFrameworkUsed() && WRONG_FUNCTIONS.contains(callee.toLowerCase(Locale.ENGLISH))) {
      String message = String.format(MESSAGE, callee, callee + "_once");
      context().newIssue(this, tree.callee(), message);
    }

  }

  private boolean isLaravelFrameworkUsed() {
    return context().getFramework() == SymbolTable.Framework.LARAVEL;
  }

}
