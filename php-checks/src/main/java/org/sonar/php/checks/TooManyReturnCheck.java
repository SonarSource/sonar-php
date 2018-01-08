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
import org.sonar.plugins.php.api.tree.ScriptTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = TooManyReturnCheck.KEY)
public class TooManyReturnCheck extends PHPVisitorCheck {

  public static final String KEY = "S1142";
  private static final String MESSAGE = "Reduce the number of returns of this function %s, down to the maximum allowed %s.";

  private static final int DEFAULT = 3;
  private final Deque<Deque<SyntaxToken>> returnStatementCounter = new ArrayDeque<>();

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT)
  int max = DEFAULT;


  @Override
  public void visitReturnStatement(ReturnStatementTree tree) {
    super.visitReturnStatement(tree);
    boolean isGlobalScope = returnStatementCounter.isEmpty();
    if (!isGlobalScope) {
      returnStatementCounter.peek().push(tree.returnToken());
    }
  }

  @Override
  public void visitScript(ScriptTree tree) {
    returnStatementCounter.clear();
    super.visitScript(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    enterFunction();
    super.visitFunctionDeclaration(tree);
    leaveFunction(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    enterFunction();
    super.visitFunctionExpression(tree);
    leaveFunction(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    enterFunction();
    super.visitMethodDeclaration(tree);
    leaveFunction(tree);
  }

  private void enterFunction() {
    returnStatementCounter.push(new ArrayDeque<>());
  }

  private void leaveFunction(FunctionTree tree) {
    Deque<SyntaxToken> thisFunctionReturns = returnStatementCounter.pop();
    if (thisFunctionReturns.size() > max) {
      String message = String.format(MESSAGE, thisFunctionReturns.size(), max);
      PreciseIssue issue = context().newIssue(this, tree.functionToken(), message);
      thisFunctionReturns.forEach(returnToken -> issue.secondary(returnToken, null));
    }
  }
}
