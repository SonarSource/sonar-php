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

import java.util.ArrayDeque;
import java.util.Deque;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = NestedFunctionDepthCheck.KEY)
public class NestedFunctionDepthCheck extends PHPVisitorCheck {

  public static final String KEY = "S2004";

  private static final String MESSAGE = "Refactor this code to not nest functions more than %s levels deep.";

  private Deque<SyntaxToken> nestedStack = new ArrayDeque<>();
  public static final int DEFAULT = 3;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  int max = DEFAULT;

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    nestedStack.clear();
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    enterFunction(tree);
    super.visitFunctionDeclaration(tree);
    exitFunction();
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    enterFunction(tree);
    super.visitMethodDeclaration(tree);
    exitFunction();
  }

  private void enterFunction(FunctionTree tree) {
    nestedStack.push(tree.functionToken());
    if (nestedStack.size() == max + 1) {
      PreciseIssue issue = context().newIssue(this, tree.functionToken(), String.format(MESSAGE, max));
      nestedStack.forEach(secondary -> issue.secondary(secondary, "Nesting +1"));
    }
  }

  private void exitFunction() {
    nestedStack.pop();
  }

}
