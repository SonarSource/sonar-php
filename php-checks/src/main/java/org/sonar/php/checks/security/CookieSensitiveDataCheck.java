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
package org.sonar.php.checks.security;

import java.util.Arrays;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.UnsetVariableStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.getLowerCaseFunctionName;

@Rule(key = "S2255")
public class CookieSensitiveDataCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure that this cookie is used safely.";

  private static final List<String> SET_COOKIE_FUNCTIONS = Arrays.asList("setcookie", "setrawcookie");
  private static final List<String> COOKIE_PREDEFINED_VARIABLES = Arrays.asList("$_COOKIE", "$HTTP_COOKIE_VARS");
  private static final int VALUE_PARAMETER_INDEX = 1;
  private long nestedUnsetStatementCount;
  private long nestedIssetStatementCount;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    nestedUnsetStatementCount = 0;
    nestedIssetStatementCount = 0;
    super.visitCompilationUnit(tree);
    nestedUnsetStatementCount = 0;
    nestedIssetStatementCount = 0;
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = getLowerCaseFunctionName(tree);
    if ("isset".equals(functionName)) {
      nestedIssetStatementCount++;
      super.visitFunctionCall(tree);
      nestedIssetStatementCount--;
      return;
    } else if (SET_COOKIE_FUNCTIONS.contains(functionName) && hasCookieValue(tree)) {
      context().newIssue(this, tree.callee(), MESSAGE);
    }

    super.visitFunctionCall(tree);
  }

  @Override
  public void visitUnsetVariableStatement(UnsetVariableStatementTree tree) {
    nestedUnsetStatementCount++;
    super.visitUnsetVariableStatement(tree);
    nestedUnsetStatementCount--;
  }

  @Override
  public void visitVariableIdentifier(VariableIdentifierTree tree) {
    super.visitVariableIdentifier(tree);

    if (COOKIE_PREDEFINED_VARIABLES.contains(tree.text()) && nestedUnsetStatementCount == 0 && nestedIssetStatementCount == 0) {
      context().newIssue(this, tree, MESSAGE);
    }
  }

  private static boolean hasCookieValue(FunctionCallTree tree) {
    return tree.arguments().size() > VALUE_PARAMETER_INDEX
      && !CheckUtils.isNullOrEmptyString(tree.arguments().get(VALUE_PARAMETER_INDEX));
  }

}
