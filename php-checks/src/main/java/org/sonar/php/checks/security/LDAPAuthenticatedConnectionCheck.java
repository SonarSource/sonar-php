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

import com.google.common.collect.ImmutableSet;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.FunctionUsageCheck;
import org.sonar.php.tree.visitors.AssignmentExpressionVisitor;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;

@Rule(key = "S4433")
public class LDAPAuthenticatedConnectionCheck extends FunctionUsageCheck {

  private static final String MESSAGE = "Provide username and password to authenticate the connection.";
  private static final int USERNAME_ARG_INDEX = 1;
  private static final int PASSWORD_ARG_INDEX = 2;
  private AssignmentExpressionVisitor assignmentExpressionVisitor;

  @Override
  protected ImmutableSet<String> functionNames() {
    return ImmutableSet.of("ldap_bind");
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    assignmentExpressionVisitor = new AssignmentExpressionVisitor(context().symbolTable());
    tree.accept(assignmentExpressionVisitor);
    super.visitCompilationUnit(tree);
  }

  @Override
  protected void createIssue(FunctionCallTree tree) {
    if (argumentIsNullOrEmptyString(tree, USERNAME_ARG_INDEX) || argumentIsNullOrEmptyString(tree, PASSWORD_ARG_INDEX)) {
      context().newIssue(this, tree, MESSAGE);
    }
  }

  private boolean argumentIsNullOrEmptyString(FunctionCallTree tree, int argumentIndex) {
    if (tree.arguments().size() > argumentIndex) {
      ExpressionTree valueArgument = getAssignedValue(tree.arguments().get(argumentIndex));
      return CheckUtils.isNullOrEmptyString(valueArgument);
    }
    return true;
  }

  private ExpressionTree getAssignedValue(ExpressionTree value) {
    if (value.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      Symbol valueSymbol = context().symbolTable().getSymbol(value);
      return assignmentExpressionVisitor
        .getUniqueAssignedValue(valueSymbol)
        .orElse(value);
    }
    return value;
  }

}
