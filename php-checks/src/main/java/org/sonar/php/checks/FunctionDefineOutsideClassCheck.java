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

import com.google.common.collect.Sets;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = FunctionDefineOutsideClassCheck.KEY)
public class FunctionDefineOutsideClassCheck extends PHPVisitorCheck {

  public static final String KEY = "S2007";
  private static final String MESSAGE = "Move this %s into a class.";

  private final Set<String> globalVariableNames = Sets.newHashSet();

  @Override
  public void visitScript(ScriptTree tree) {
    globalVariableNames.clear();
    super.visitScript(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    // don't visit nested nodes
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    // don't visit nested nodes
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    context().newIssue(this, tree.name(), String.format(MESSAGE, "function"));
    // don't visit nested nodes
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    super.visitAssignmentExpression(tree);

    if (tree.is(Kind.ASSIGNMENT) && tree.variable().is(Kind.VARIABLE_IDENTIFIER)) {
      String varName = ((VariableIdentifierTree) tree.variable()).variableExpression().text();

      if (!isSuperGlobal(varName) && !globalVariableNames.contains(varName)) {
        context().newIssue(this, tree.variable(), String.format(MESSAGE, "variable"));
        globalVariableNames.add(varName);
      }
    }
  }

  private static boolean isSuperGlobal(String varName) {
    return "$GLOBALS".equals(varName) || CheckUtils.SUPERGLOBALS_BY_OLD_NAME.values().contains(varName);
  }

}
