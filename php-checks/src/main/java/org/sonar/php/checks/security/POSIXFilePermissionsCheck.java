/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2020 SonarSource SA
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
import org.apache.commons.lang.StringUtils;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.tree.visitors.AssignmentExpressionVisitor;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.SeparatedList;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.CallArgumentTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.MemberAccessTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S2612")
public class POSIXFilePermissionsCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Make sure this permission is safe.";

  private AssignmentExpressionVisitor assignmentExpressionVisitor;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    assignmentExpressionVisitor = new AssignmentExpressionVisitor(context().symbolTable());
    tree.accept(assignmentExpressionVisitor);
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    ExpressionTree callee = tree.callee();
    if (callee.is(Kind.OBJECT_MEMBER_ACCESS)) {
      MemberAccessTree chmodAccessTree = (MemberAccessTree) callee;
      if (chmodAccessTree.member().toString().equals("chmod")) {
        chmodCheck(tree, tree.callArguments());
      }
    }
    if (callee.toString().equals("chmod")) {
      chmodCheck(tree, tree.callArguments());
    }
    if (callee.toString().equals("umask")) {
      umaskCheck(tree, tree.callArguments());
    }
    super.visitFunctionCall(tree);
  }

  private void chmodCheck(FunctionCallTree tree, SeparatedList<CallArgumentTree> arguments) {
    if (arguments.size() >= 2) {
      int mode = resolveArgument(arguments.get(1), 0);
      int umask = arguments.size() >= 3 ? resolveArgument(arguments.get(2), 0) : 0;

      if ((mode & ~umask) % 8 != 0) {
        context().newIssue(this, tree, MESSAGE);
      }
    }
  }

  private void umaskCheck(FunctionCallTree tree, SeparatedList<CallArgumentTree> arguments) {
    if (!arguments.isEmpty()) {
      int mask = resolveArgument(arguments.get(0), 7);
      if (mask % 8 != 7) {
        context().newIssue(this, tree, MESSAGE);
      }
    }
  }

  private int resolveArgument(CallArgumentTree modeArgument, int defaultValue) {
    if (modeArgument.value().is(Kind.VARIABLE_IDENTIFIER)) {
      Symbol symbol = context().symbolTable().getSymbol(modeArgument.value());
      Optional<ExpressionTree> uniqueAssignedValue = assignmentExpressionVisitor.getUniqueAssignedValue(symbol);
      if (uniqueAssignedValue.isPresent()) {
        return getDecimalRepresentation(uniqueAssignedValue.get().toString(), defaultValue);
      }
    }
    return getDecimalRepresentation(modeArgument.value().toString(), defaultValue);
  }

  private static int getDecimalRepresentation(String argument, int defaultValue) {
    int decimalValue = defaultValue;
    if (argument.matches("\"[0-9]*\"")) {
      decimalValue = Integer.valueOf(CheckUtils.trimQuotes(argument));
    } else if (argument.matches("^0[0-7]*$")) {
      decimalValue = Integer.parseInt(argument, 8);
    } else if (StringUtils.isNumeric(argument)) {
      decimalValue = Integer.valueOf(argument);
    }
    return decimalValue;
  }

}
