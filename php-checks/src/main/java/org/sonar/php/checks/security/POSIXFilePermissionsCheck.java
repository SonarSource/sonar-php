/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2612")
public class POSIXFilePermissionsCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure this permission is safe.";

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    String functionName = CheckUtils.lowerCaseFunctionName(tree);
    if (tree.callee().is(Kind.OBJECT_MEMBER_ACCESS)) {
      if ("chmod".equals(functionName)) {
        chmodSymfonyAndLaravelCheck(tree);
      }
    } else if ("chmod".equals(functionName)) {
      chmodCoreCheck(tree);
    } else if ("umask".equals(functionName)) {
      umaskCheck(tree);
    }
    super.visitFunctionCall(tree);
  }

  private void chmodCoreCheck(FunctionCallTree tree) {
    Optional<CallArgumentTree> permissionsArgument = CheckUtils.argument(tree, "permissions", 1);
    int mode = permissionsArgument.isPresent() ? resolveArgument(permissionsArgument.get(), 0) : 0;
    if (mode % 8 != 0) {
      context().newIssue(this, tree, MESSAGE);
    }
  }

  private void chmodSymfonyAndLaravelCheck(FunctionCallTree tree) {
    Optional<CallArgumentTree> modeArgument = CheckUtils.argument(tree, "mode", 1);
    Optional<CallArgumentTree> umaskArgument = CheckUtils.argument(tree, "umask", 2);
    int mode = modeArgument.isPresent() ? resolveArgument(modeArgument.get(), 0) : 0;
    int umask = umaskArgument.isPresent() ? resolveArgument(umaskArgument.get(), 0) : 0;

    if ((mode & ~umask) % 8 != 0) {
      context().newIssue(this, tree, MESSAGE);
    }
  }

  private void umaskCheck(FunctionCallTree tree) {
    Optional<CallArgumentTree> maskArgument = CheckUtils.argument(tree, "mask", 0);
    int mask = maskArgument.isPresent() ? resolveArgument(maskArgument.get(), 7) : 7;
    if (mask % 8 != 7) {
      context().newIssue(this, tree, MESSAGE);
    }
  }

  private int resolveArgument(CallArgumentTree argument, int defaultValue) {
    ExpressionTree argumentExpressionTree = CheckUtils.assignedValue(argument.value());
    if (argumentExpressionTree.is(Kind.REGULAR_STRING_LITERAL, Kind.NUMERIC_LITERAL)) {
      String literal = ((LiteralTree) argumentExpressionTree).value();
      return getDecimalRepresentation(literal, defaultValue);
    }
    return defaultValue;
  }

  private static int getDecimalRepresentation(String argument, int defaultValue) {
    if (argument.matches("\"[0-9]*\"")) {
      return Integer.valueOf(CheckUtils.trimQuotes(argument));
    } else if (argument.matches("^0[0-7]*$")) {
      return Integer.parseInt(argument, 8);
    }
    try {
      return Integer.parseInt(argument);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

}
