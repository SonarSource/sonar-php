/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.php.tree.symbols;

import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.statement.NamespaceStatementTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.sonar.plugins.php.api.symbols.Symbol.Kind.FUNCTION;

public class DeclarationVisitor extends PHPVisitorCheck {

  private final SymbolTableImpl symbolTable;

  private SymbolQualifiedName currentNamespace = SymbolQualifiedName.GLOBAL_NAMESPACE;
  private Scope globalScope;

  DeclarationVisitor(SymbolTableImpl symbolTable) {
    this.symbolTable = symbolTable;
  }

  @Override
  public void visitCompilationUnit(CompilationUnitTree tree) {
    globalScope = symbolTable.addScope(new Scope(tree));
    super.visitCompilationUnit(tree);
  }

  @Override
  public void visitNamespaceStatement(NamespaceStatementTree tree) {
    NamespaceNameTree namespaceNameTree = tree.namespaceName();
    currentNamespace = namespaceNameTree != null ? SymbolQualifiedName.create(namespaceNameTree) : SymbolQualifiedName.GLOBAL_NAMESPACE;
    super.visitNamespaceStatement(tree);

    boolean isBracketedNamespace = tree.openCurlyBrace() != null;
    if (isBracketedNamespace) {
      currentNamespace = SymbolQualifiedName.GLOBAL_NAMESPACE;
    }
  }

  @Override
  public void visitClassDeclaration(ClassDeclarationTree tree) {
    symbolTable.declareTypeSymbol(tree.name(), globalScope, currentNamespace);
    super.visitClassDeclaration(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    symbolTable.declareSymbol(tree.name(), FUNCTION, globalScope, currentNamespace);
    super.visitFunctionDeclaration(tree);
  }
}
