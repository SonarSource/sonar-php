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
package org.sonar.php.checks.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.VariableDeclarationTree;
import org.sonar.plugins.php.api.tree.expression.ArrayAssignmentPatternElementTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionExpressionTree;
import org.sonar.plugins.php.api.tree.expression.LexicalVariablesTree;
import org.sonar.plugins.php.api.tree.expression.VariableIdentifierTree;
import org.sonar.plugins.php.api.tree.expression.VariableTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class ReadWriteUsages {

  private final SymbolTable symbolTable;
  private final Set<SyntaxToken> writes = new HashSet<>();
  private final Set<SyntaxToken> readAssignment = new HashSet<>();
  private final Set<SyntaxToken> declarations = new HashSet<>();
  private final ListMultimap<Symbol,Symbol> inheritedVariablesByParent = ArrayListMultimap.create();
  private final Map<Symbol,Symbol> parentSymbolByInheritedReference = new HashMap<>();

  public ReadWriteUsages(Tree tree, SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
    tree.accept(new UsageVisitor());
  }

  public boolean isRead(Symbol symbol) {
    return hasReadUsage(symbol)
      || inheritedVariablesByParent.get(symbol).stream().anyMatch(this::isRead)
      || hasParentWhichIsRead(symbol);
  }

  private boolean hasReadUsage(Symbol symbol) {
    List<SyntaxToken> allReferences = new ArrayList<>();
    allReferences.add(symbol.declaration().token());
    allReferences.addAll(symbol.usages());
    return allReferences.stream()
      .anyMatch(t -> (!writes.contains(t) && !declarations.contains(t)) || readAssignment.contains(t));
  }

  private boolean hasParentWhichIsRead(Symbol symbol) {
    Symbol parent = parentSymbolByInheritedReference.get(symbol);
    return parent != null && hasReadUsage(parent);
  }

  private class UsageVisitor extends PHPVisitorCheck {
    @Override
    public void visitVariableDeclaration(VariableDeclarationTree tree) {
      visitAssignedVariable(tree.identifier());
      super.visitVariableDeclaration(tree);
    }

    @Override
    public void visitAssignmentExpression(AssignmentExpressionTree tree) {
      if (!tree.getParent().is(Kind.EXPRESSION_STATEMENT) && !tree.operator().startsWith("=")) {
        // compound assignment used as operand
        visitReadAssignedVariable(tree.variable());
      } else {
        visitAssignedVariable(tree.variable());
      }
      super.visitAssignmentExpression(tree);
    }

    @Override
    public void visitArrayAssignmentPatternElement(ArrayAssignmentPatternElementTree tree) {
      visitAssignedVariable(tree.variable());
      super.visitArrayAssignmentPatternElement(tree);
    }

    @Override
    public void visitForEachStatement(ForEachStatementTree tree) {
      ExpressionTree key = tree.key();
      if (key != null) {
        visitAssignedVariable(key);
      }
      visitAssignedVariable(tree.value());
      super.visitForEachStatement(tree);
    }

    private void visitAssignedVariable(Tree tree) {
      if (!tree.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        return;
      }
      writes.add(((VariableIdentifierTree) tree).token());
    }

    private void visitReadAssignedVariable(Tree tree) {
      if (!tree.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        return;
      }
      SyntaxToken token = ((VariableIdentifierTree) tree).token();
      writes.add(token);
      readAssignment.add(token);
    }

    @Override
    public void visitFunctionExpression(FunctionExpressionTree tree) {
      LexicalVariablesTree lexicalVars = tree.lexicalVars();
      if (lexicalVars != null) {
        Scope scope = symbolTable.getScopeFor(tree);

        for (VariableTree variableTree : lexicalVars.variables()) {
          visitLexicalVar(scope, variableTree);
        }
      }

      super.visitFunctionExpression(tree);
    }

    private void visitLexicalVar(Scope scope, VariableTree variableTree) {
      Scope parentScope = scope.outer();
      VariableIdentifierTree variableIdentifier = null;

      if (variableTree.is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        variableIdentifier = (VariableIdentifierTree) variableTree;
        Symbol parentScopeSymbol = parentScope.getSymbol(variableIdentifier.text());
        Symbol symbol = scope.getSymbol(variableIdentifier.text());

        if (parentScopeSymbol != null && symbol != null) {
          inheritedVariablesByParent.put(parentScopeSymbol, symbol);
        }

      } else if (variableTree.is(Tree.Kind.REFERENCE_VARIABLE) && variableTree.variableExpression().is(Tree.Kind.VARIABLE_IDENTIFIER)) {
        variableIdentifier = (VariableIdentifierTree) variableTree.variableExpression();
        Symbol parentScopeSymbol = parentScope.getSymbol(variableIdentifier.text());
        Symbol symbol = scope.getSymbol(variableIdentifier.text());

        if (parentScopeSymbol != null && symbol != null) {
          inheritedVariablesByParent.put(parentScopeSymbol, symbol);
          parentSymbolByInheritedReference.put(symbol, parentScopeSymbol);
        }
      }

      if (variableIdentifier != null) {
        declarations.add(variableIdentifier.token());
      }
    }

  }

}
