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
package org.sonar.php.checks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import org.sonar.check.Rule;
import org.sonar.php.cfg.LiveVariablesAnalysis;
import org.sonar.plugins.php.api.cfg.ControlFlowGraph;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.expression.*;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import sun.tools.tree.UnaryExpression;

@Rule(key = "S1226")
public class ParameterReassignedToCheck extends PHPVisitorCheck {
  private static final String MESSAGE = "Introduce a new variable instead of reusing the parameter \"%s\".";
  private static final String MESSAGE_SECONDARY = "Initial value.";

  private static final Tree.Kind[] ASSIGNMENT_LIKE_UNARY={
    Tree.Kind.PREFIX_DECREMENT,
    Tree.Kind.PREFIX_INCREMENT,
    Tree.Kind.POSTFIX_DECREMENT,
    Tree.Kind.POSTFIX_INCREMENT
  };

  private final Set<Symbol> variables = new HashSet<>();

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    Set<Symbol> symbols = visitFunctionLikeDeclaration(tree);

    super.visitFunctionDeclaration(tree);

    symbols.forEach(variables::remove);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    Set<Symbol> symbols = visitFunctionLikeDeclaration(tree);

    super.visitMethodDeclaration(tree);

    symbols.forEach(variables::remove);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    Set<Symbol> symbols = visitFunctionLikeDeclaration(tree);

    super.visitFunctionExpression(tree);

    symbols.forEach(variables::remove);
  }

  private Set<Symbol> visitFunctionLikeDeclaration(FunctionTree tree) {
    Set<Symbol> symbols = new HashSet<>();

    for (ParameterTree parameter : tree.parameters().parameters()) {
      Symbol symbol = context().symbolTable().getSymbol(parameter.variableIdentifier());
      if (symbol != null) {
        symbols.add(symbol);
        variables.add(symbol);
      }
    }

    return symbols;
  }

  @Override
  public void visitForEachStatement(ForEachStatementTree tree) {
    ExpressionTree variable = tree.value();
    Symbol symbol = null;

    if (variable.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      symbol = context().symbolTable().getSymbol(variable);
      if (symbol != null) {
        variables.add(symbol);
      }
    }

    super.visitForEachStatement(tree);

    if (symbol != null) {
      variables.remove(symbol);
    }
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    visitWritingExpression(tree, tree.variable());

    super.visitAssignmentExpression(tree);
  }

  @Override
  public void visitPrefixExpression(UnaryExpressionTree tree) {
    if (tree.is(ASSIGNMENT_LIKE_UNARY)) {
      visitWritingExpression(tree, tree.expression());
    }

    super.visitPrefixExpression(tree);
  }

  @Override
  public void visitPostfixExpression(UnaryExpressionTree tree) {
    if (tree.is(ASSIGNMENT_LIKE_UNARY)) {
      visitWritingExpression(tree, tree.expression());
    }

    super.visitPostfixExpression(tree);
  }

  private void visitWritingExpression(ExpressionTree tree, ExpressionTree expression) {
    if (expression.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      IdentifierTree identifier = (VariableIdentifierTree) expression;
      Symbol reference = context().symbolTable().getSymbol(identifier);
      if (reference != null && variables.contains(reference)) {
        context().newIssue(this, tree, String.format(MESSAGE, identifier.text())).secondary(reference.declaration(), MESSAGE_SECONDARY);
      }
    }
  }
}
