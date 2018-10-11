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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.ReadWriteUsages;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.ReferenceVariableTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.statement.CatchBlockTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = UnusedLocalVariableCheck.KEY)
public class UnusedLocalVariableCheck extends PHPVisitorCheck {

  public static final String KEY = "S1481";
  private static final String MESSAGE = "Remove this unused \"%s\" local variable.";

  private List<IdentifierTree> exclusions = new ArrayList<>();
  private Set<Tree> raisedIssueLocations = new HashSet<>();
  private ReadWriteUsages usages;

  @Override
  public void visitFunctionExpression(FunctionExpressionTree tree) {
    if (tree.lexicalVars() != null) {
      Scope parentScope = context().symbolTable().getScopeFor(tree).outer();

      for (VariableTree variableTree : tree.lexicalVars().variables()) {

        if (variableTree.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
          VariableIdentifierTree variableIdentifier = (VariableIdentifierTree) variableTree;
          Symbol parentScopeSymbol = parentScope.getSymbol(variableIdentifier.text());

          if (parentScopeSymbol == null) {
            exclusions.add(variableIdentifier);
          }
        }
      }
    }

    super.visitFunctionExpression(tree);
  }

  @Override
  public void visitFunctionCall(FunctionCallTree tree) {
    for (ExpressionTree argument : tree.arguments()) {
      if (argument.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        exclusions.add((IdentifierTree) argument);
      }
    }
    super.visitFunctionCall(tree);
  }

  @Override
  public void visitCatchBlock(CatchBlockTree tree) {
    exclusions.add(tree.variable());
    super.visitCatchBlock(tree);
  }

  @Override
  public void visitForEachStatement(ForEachStatementTree tree) {
    if (tree.key() != null) {
      if (tree.value().is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        exclusions.add((IdentifierTree) tree.value());

      } else if (tree.value().is(Tree.Kind.REFERENCE_VARIABLE) && ((ReferenceVariableTree) tree.value()).variableExpression().is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        exclusions.add((IdentifierTree) ((ReferenceVariableTree) tree.value()).variableExpression());
      }
    }
    super.visitForEachStatement(tree);
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    exclusions.clear();
    raisedIssueLocations.clear();
    usages = new ReadWriteUsages(tree, context().symbolTable());
    super.visitCompilationUnit(tree);
    for (Scope scope : context().symbolTable().getScopes()) {
      if (CheckUtils.isFunction(scope.tree()) && !scope.hasUnresolvedCompact()) {
        checkScope(scope);
      }
    }
  }

  private void checkScope(Scope scope) {
    for (Symbol symbol : scope.getSymbols(Symbol.Kind.VARIABLE)) {
      // symbol should be declared in this scope
      if (symbol.scope().equals(scope) && !usages.isRead(symbol) && !exclusions.contains(symbol.declaration())
        && !raisedIssueLocations.contains(symbol.declaration())) {

        context().newIssue(this, symbol.declaration(), String.format(MESSAGE, symbol.name()));
        raisedIssueLocations.add(symbol.declaration());
      }
    }
  }

}
