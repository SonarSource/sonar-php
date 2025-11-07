/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.Locale;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = RequireIncludeInstructionsUsageCheck.KEY)
public class RequireIncludeInstructionsUsageCheck extends PHPVisitorCheck {

  public static final String KEY = "S4833";
  private static final String MESSAGE = "Replace \"%s\" with namespace import mechanism through the \"use\" keyword.";

  private static final Set<String> EXCLUDED_FILES = Set.of("autoload.php", "ScriptHandler.php");
  private static final Set<String> WRONG_FUNCTIONS = Set.of(
    "require",
    "include",
    "require_once",
    "include_once");

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    if (!isExcludedFile()) {
      super.visitCompilationUnit(tree);
    }
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    super.visitFunctionCall(tree);

    String callee = tree.callee().toString();
    if (!isLaravelFrameworkUsed() && WRONG_FUNCTIONS.contains(callee.toLowerCase(Locale.ENGLISH)) && !isAutoloadImport(tree)) {
      String message = String.format(MESSAGE, callee);
      context().newIssue(this, tree.callee(), message);
    }

  }

  private boolean isExcludedFile() {
    String filename = context().getPhpFile().filename();
    return EXCLUDED_FILES.contains(filename);
  }

  private static boolean isAutoloadImport(FunctionCallTree tree) {
    String call = tree.toString();
    return (call.startsWith("include") || call.startsWith("require")) && call.endsWith("autoload.php'");
  }

  private boolean isLaravelFrameworkUsed() {
    return context().getFramework() == SymbolTable.Framework.LARAVEL;
  }
}
