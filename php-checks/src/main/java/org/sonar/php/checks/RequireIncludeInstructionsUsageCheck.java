/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import com.google.common.collect.ImmutableSet;
import java.util.Locale;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = RequireIncludeInstructionsUsageCheck.KEY)
public class RequireIncludeInstructionsUsageCheck extends PHPVisitorCheck {

  public static final String KEY = "S4833";
  private static final String MESSAGE = "Replace \"%s\" with namespace import mechanism through the \"use\" keyword.";

  private static final Set<String> EXCLUDED_FILES = ImmutableSet.of("autoload.php", "ScriptHandler.php");
  private static final Set<String> WRONG_FUNCTIONS = ImmutableSet.of(
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

    if (WRONG_FUNCTIONS.contains(callee.toLowerCase(Locale.ENGLISH))) {
      String message = String.format(MESSAGE, callee);
      context().newIssue(this, tree.callee(), message);
    }
  }

  private boolean isExcludedFile() {
    String filename = context().getPhpFile().filename();
    return EXCLUDED_FILES.contains(filename);
  }
}
