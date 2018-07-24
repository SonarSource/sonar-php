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
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.php.checks.utils.CheckUtils.getFunctionName;

@Rule(key = "S2255")
public class CookieSensitiveDataCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure storing this data in this cookie is safe here.";

  private static final List<String> SET_COOKIE_FUNCTIONS = Arrays.asList("setcookie", "setrawcookie");
  private static final int SET_COOKIE_VALUE_PARAMETER = 1;

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = getFunctionName(tree);
    if (SET_COOKIE_FUNCTIONS.contains(functionName) && !argumentIsNullOrEmpty(tree, SET_COOKIE_VALUE_PARAMETER)) {
      context().newIssue(this, tree.callee(), MESSAGE);
    }

    super.visitFunctionCall(tree);
  }

  private static boolean argumentIsNullOrEmpty(FunctionCallTree tree, int argumentIndex) {
    if (tree.arguments().size() > argumentIndex) {
      ExpressionTree valueArgument = tree.arguments().get(argumentIndex);
      return isEmpty(valueArgument);
    }
    return true;
  }

  private static boolean isEmpty(ExpressionTree tree) {
    if (tree.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      String value = CheckUtils.trimQuotes(((LiteralTree) tree).value());
      return value.trim().isEmpty();
    }
    return false;
  }
}
