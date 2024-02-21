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
package org.sonar.php.checks;

import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.cfg.LiveVariablesAnalysis;
import org.sonar.plugins.php.api.cfg.ControlFlowGraph;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ParameterTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1226")
public class ReassignedBeforeUsedCheck extends PHPVisitorCheck {
  private static final String MESSAGE = "Introduce a new variable instead of reusing the parameter \"%s\".";
  private static final String SECONDARY_MESSAGE = "Initial value.";

  private final Set<Symbol> investigatedParameters = new HashSet<>();

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    investigatedParameters.clear();
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    visitFunctionTree(tree);
    super.visitFunctionDeclaration(tree);
  }

  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    visitFunctionTree(tree);
    super.visitMethodDeclaration(tree);
  }

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    visitFunctionTree(tree);
    super.visitFunctionExpression(tree);
  }

  private void visitFunctionTree(FunctionTree tree) {
    ControlFlowGraph cfg = ControlFlowGraph.build(tree, context());
    if (cfg == null) {
      return;
    }

    LiveVariablesAnalysis analysis = LiveVariablesAnalysis.analyze(cfg, context().symbolTable());
    Set<Symbol> live = analysis.getLiveVariables(cfg.start()).getIn();
    for (ParameterTree parameterTree : tree.parameters().parameters()) {
      if (parameterTree.referenceToken() == null) {
        Symbol symbol = context().symbolTable().getSymbol(parameterTree.variableIdentifier());
        if (!live.contains(symbol)) {
          investigatedParameters.add(symbol);
        }
      }
    }
  }

  @Override
  public void visitAssignmentExpression(AssignmentExpressionTree tree) {
    if (tree.variable().is(Tree.Kind.VARIABLE_IDENTIFIER)) {
      VariableIdentifierTree identifier = (VariableIdentifierTree) tree.variable();
      Symbol reference = context().symbolTable().getSymbol(identifier);
      if (reference != null && (reference.is(Symbol.Kind.PARAMETER) || reference.is(Symbol.Kind.VARIABLE)) && investigatedParameters.contains(reference)) {
        context().newIssue(this, tree, String.format(MESSAGE, reference.name())).secondary(reference.declaration(), SECONDARY_MESSAGE);
        investigatedParameters.remove(reference);
      }
    }

    super.visitAssignmentExpression(tree);
  }
}
