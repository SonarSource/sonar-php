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

import java.util.Deque;
import java.util.LinkedList;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.SyntacticEquivalence;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ReturnStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = UselessIncrementCheck.KEY)
public class UselessIncrementCheck extends PHPVisitorCheck {

  public static final String KEY = "S2123";
  private Deque<Scope> scopes = new LinkedList<>();

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    // skip anonymous functions
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    scopes.clear();
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    scopes.push(context().symbolTable().getScopeFor(tree));
    super.visitMethodDeclaration(tree);
    scopes.pop();
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    scopes.push(context().symbolTable().getScopeFor(tree));
    super.visitClassDeclaration(tree);
    scopes.pop();
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    scopes.push(context().symbolTable().getScopeFor(tree));
    super.visitFunctionDeclaration(tree);
    scopes.pop();
  }

  @Override
  public void visitReturnStatement(ReturnStatementTree tree) {
    ExpressionTree returnedExpression = tree.expression();
    if (returnedExpression != null) {
      returnedExpression = CheckUtils.skipParenthesis(returnedExpression);
      if (returnedExpression.is(Tree.Kind.POSTFIX_INCREMENT, Tree.Kind.POSTFIX_DECREMENT)) {
        ExpressionTree postfixedExpression = CheckUtils.skipParenthesis(((UnaryExpressionTree) returnedExpression).expression());
        if (postfixedExpression.is(Tree.Kind.VARIABLE_IDENTIFIER) && isFromCurrentScope((VariableIdentifierTree) postfixedExpression)) {
          reportIssue(returnedExpression);
        }
      }
    }
    super.visitReturnStatement(tree);
  }

  private boolean isFromCurrentScope(VariableIdentifierTree variableIdentifierTree) {
    SyntaxToken variableToken = variableIdentifierTree.token();
    Scope currentScope = scopes.peek();
    return currentScope != null && currentScope.getSymbols(Symbol.Kind.VARIABLE).stream()
      .filter(symbol -> !(symbol.hasModifier("static") || symbol.hasModifier("global")))
      .filter(symbol -> currentScope.equals(symbol.scope()))
      .flatMap(symbol -> symbol.usages().stream())
      .anyMatch(variableToken::equals);
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    if (tree.is(Tree.Kind.ASSIGNMENT)) {
      ExpressionTree expression = CheckUtils.skipParenthesis(tree.value());
      if (expression.is(Tree.Kind.POSTFIX_INCREMENT, Tree.Kind.POSTFIX_DECREMENT)
        && SyntacticEquivalence.areSyntacticallyEquivalent(tree.variable(), ((UnaryExpressionTree) expression).expression())) {
        reportIssue(expression);
      }
    }
    super.visitAssignmentExpression(tree);
  }

  private void reportIssue(ExpressionTree expression) {
    String message = String.format("Remove this %s or correct the code not to waste it.", expression.is(Tree.Kind.POSTFIX_INCREMENT) ? "increment" : "decrement");
    context().newIssue(this, expression, message);
  }

}
