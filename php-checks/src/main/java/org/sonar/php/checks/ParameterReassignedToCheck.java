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
import java.util.Set;

import org.sonar.check.Rule;
import org.sonar.php.cfg.LiveVariablesAnalysis;
import org.sonar.plugins.php.api.cfg.ControlFlowGraph;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.UnaryExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1226")
public class ParameterReassignedToCheck extends PHPVisitorCheck {
  private static final String MESSAGE = "Introduce a new variable instead of reusing the parameter \"%s\".";
  private static final String SECONDARY_MESSAGE = "Initial value.";

  private final Set<Symbol> variables = new HashSet<>();

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    Set<Symbol> live = visitFunctionTree(tree);
    super.visitFunctionDeclaration(tree);
    clearFixFunctionTree(tree, live);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    Set<Symbol> live = visitFunctionTree(tree);
    super.visitMethodDeclaration(tree);
    clearFixFunctionTree(tree, live);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    Set<Symbol> live = visitFunctionTree(tree);
    super.visitFunctionExpression(tree);
    clearFixFunctionTree(tree, live);
  }

  private Set<Symbol> visitFunctionTree(FunctionTree tree) {
    ControlFlowGraph cfg = ControlFlowGraph.build(tree, context());
    if (cfg == null) {
      return new HashSet<>();
    }

    LiveVariablesAnalysis analysis = LiveVariablesAnalysis.analyze(cfg, context().symbolTable());
    Set<Symbol> live = analysis.getLiveVariables(cfg.start()).getIn();
    for (ParameterTree parameterTree : tree.parameters().parameters()) {
      if (parameterTree.referenceToken() == null) {
        Symbol symbol = context().symbolTable().getSymbol(parameterTree.variableIdentifier());
        if (!live.contains(symbol)) {
          variables.add(symbol);
        }
      }
    }
    return live;
  }

  private void clearFixFunctionTree(FunctionTree tree, Set<Symbol> live) {
    for (ParameterTree parameterTree : tree.parameters().parameters()) {
      Symbol symbol = context().symbolTable().getSymbol(parameterTree.variableIdentifier());
      if (!live.contains(symbol)) {
        variables.remove(symbol);
      }
    }
  }

  @Override
  public void visitForEachStatement(ForEachStatementTree tree) {
    ControlFlowGraph cfg = ControlFlowGraph.build(tree, context());
    if (cfg == null) {
      return;
    }

    Symbol symbol = context().symbolTable().getSymbol(tree.value());
    LiveVariablesAnalysis analysis = LiveVariablesAnalysis.analyze(cfg, context().symbolTable());

    Set<Symbol> live = analysis.getLiveVariables(cfg.start()).getIn();
    boolean liveVar = live.contains(symbol);

    if (!liveVar) {
      variables.add(symbol);
    }

    super.visitForEachStatement(tree);

    if (!liveVar) {
      variables.remove(symbol);
    }
  }

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    ControlFlowGraph cfg = ControlFlowGraph.build(tree, context());
    if (cfg == null) {
      return;
    }

    Symbol symbol = context().symbolTable().getSymbol(tree.variable());
    LiveVariablesAnalysis analysis = LiveVariablesAnalysis.analyze(cfg, context().symbolTable());

    Set<Symbol> live = analysis.getLiveVariables(cfg.start()).getIn();
    boolean liveVar = live.contains(symbol);

    if (!liveVar) {
      variables.add(symbol);
    }

    super.visitCatchBlock(tree);

    if (!liveVar) {
      variables.remove(symbol);
    }
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    visitOverridingExpression(tree, tree.variable());

    super.visitAssignmentExpression(tree);
  }

  @Override
  public void visitPostfixExpression(UnaryExpressionTree tree) {
    visitOverridingExpression(tree, tree.expression());

    super.visitPostfixExpression(tree);
  }

  @Override
  public void visitPrefixExpression(UnaryExpressionTree tree) {
    if (tree.is(Tree.Kind.PREFIX_INCREMENT) || tree.is(Tree.Kind.PREFIX_DECREMENT)) {
      visitOverridingExpression(tree, tree.expression());
    }

    super.visitPrefixExpression(tree);
  }

  private void visitOverridingExpression(Tree tree, ExpressionTree expression) {
    if (expression.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      VariableIdentifierTree identifier = (VariableIdentifierTree) expression;
      Symbol reference = context().symbolTable().getSymbol(identifier);
      if (reference != null && (reference.is(Symbol.Kind.PARAMETER) || reference.is(Symbol.Kind.VARIABLE)) && variables.contains(reference)) {
        context().newIssue(this, tree, String.format(MESSAGE, reference.toString())).secondary(reference.declaration(), SECONDARY_MESSAGE);
        variables.remove(reference);
      }
    }
  }
}
