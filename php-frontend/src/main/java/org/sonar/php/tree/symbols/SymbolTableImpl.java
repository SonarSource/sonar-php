/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.php.symbols.ClassSymbolData;
import org.sonar.php.symbols.FunctionSymbolData;
import org.sonar.php.symbols.ProjectSymbolData;
import org.sonar.php.tree.visitors.AssignmentExpressionVisitor;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.symbols.TypeSymbol;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.visitors.PhpFile;

public class SymbolTableImpl implements SymbolTable {

  private List<Symbol> symbols = new ArrayList<>();
  private Map<Tree, Scope> scopes = new HashMap<>();
  private Map<Tree, Symbol> symbolsByTree = new HashMap<>();
  private Map<QualifiedName, Symbol> symbolByQualifiedName = new HashMap<>();
  private Collection<ClassSymbolData> classSymbolData;
  private Collection<FunctionSymbolData> functionSymbolData;

  private SymbolTableImpl() {
  }

  public static SymbolTableImpl create(CompilationUnitTree compilationUnit) {
    return create(compilationUnit, new ProjectSymbolData(), null);
  }

  public static SymbolTableImpl create(CompilationUnitTree compilationUnit, ProjectSymbolData projectSymbolData, @Nullable PhpFile file) {
    SymbolTableImpl symbolModel = new SymbolTableImpl();
    DeclarationVisitor declarationVisitor = new DeclarationVisitor(symbolModel, projectSymbolData, file);
    declarationVisitor.visitCompilationUnit(compilationUnit);
    symbolModel.classSymbolData = declarationVisitor.classSymbolData();
    symbolModel.functionSymbolData = declarationVisitor.functionSymbolData();
    new SymbolVisitor(symbolModel).visitCompilationUnit(compilationUnit);
    new SymbolUsageVisitor(
      symbolModel,
      declarationVisitor.classSymbolIndex(),
      declarationVisitor.functionSymbolIndex()
    ).visitCompilationUnit(compilationUnit);
    compilationUnit.accept(new AssignmentExpressionVisitor());
    return symbolModel;
  }

  Scope addScope(Scope scope) {
    return scopes.computeIfAbsent(scope.tree(), t -> scope);
  }

  @Override
  public Set<Scope> getScopes() {
    return new HashSet<>(scopes.values());
  }

  @Nullable
  @Override
  public Scope getScopeFor(Tree tree) {
    return scopes.get(tree);
  }

  SymbolImpl declareSymbol(IdentifierTree name, Symbol.Kind kind, Scope scope, SymbolQualifiedName namespace) {
    SymbolImpl symbol;
    if (kind.hasQualifiedName()) {
      SymbolQualifiedName qualifiedName = namespace.resolve(name.text());
      symbol = new SymbolImpl(name, kind, scope, qualifiedName);
      symbolByQualifiedName.put(qualifiedName, symbol);
    } else {
      symbol = new SymbolImpl(name, kind, scope);
    }
    addSymbol(name, scope, symbol);
    return symbol;
  }

  private void addSymbol(IdentifierTree name, Scope scope, Symbol symbol) {
    symbols.add(symbol);
    scope.addSymbol(symbol);
    associateSymbol(name, symbol);
  }

  TypeSymbolImpl declareTypeSymbol(IdentifierTree name, Scope scope, SymbolQualifiedName qualifiedName) {
    TypeSymbolImpl symbol = new TypeSymbolImpl(name, scope, qualifiedName);
    symbolByQualifiedName.put(qualifiedName, symbol);
    addSymbol(name, scope, symbol);
    return symbol;
  }

  MemberSymbolImpl declareMemberSymbol(IdentifierTree name, Symbol.Kind kind, Scope scope, TypeSymbol owner) {
    MemberSymbolImpl memberSymbol = new MemberSymbolImpl(name, kind, scope, owner);
    symbolByQualifiedName.put(memberSymbol.qualifiedName(), memberSymbol);
    addSymbol(name, scope, memberSymbol);
    return memberSymbol;
  }

  SymbolImpl createUndeclaredSymbol(QualifiedName fullyQualifiedName, Symbol.Kind kind) {
    UndeclaredSymbol undeclaredSymbol = new UndeclaredSymbol(fullyQualifiedName, kind);
    symbolByQualifiedName.put(fullyQualifiedName, undeclaredSymbol);
    return undeclaredSymbol;
  }

  /**
   * Returns all symbols in script
   */
  public List<Symbol> getSymbols() {
    return Collections.unmodifiableList(symbols);
  }

  /**
   * @param kind kind of symbols to look for
   * @return list of symbols with the given kind
   */
  @Override
  public List<Symbol> getSymbols(Symbol.Kind kind) {
    List<Symbol> result = new ArrayList<>();
    for (Symbol symbol : getSymbols()) {
      if (kind.equals(symbol.kind())) {
        result.add(symbol);
      }
    }
    return result;
  }

  /**
   * @param name name of symbols to look for
   * @return list of symbols with the given name
   */
  public List<Symbol> getSymbols(String name) {
    List<Symbol> result = new ArrayList<>();
    for (Symbol symbol : getSymbols()) {
      if (symbol.called(name)) {
        result.add(symbol);
      }
    }
    return result;
  }

  Symbol getSymbol(QualifiedName qualifiedName) {
    return symbolByQualifiedName.get(qualifiedName);
  }

  Symbol getSymbol(String qualifiedName) {
    return getSymbol(SymbolQualifiedName.qualifiedName(qualifiedName));
  }

  void associateSymbol(Tree identifier, Symbol symbol) {
    // If a tree could semantically be associated with two different symbols (see SONARPHP-857), keep the first one and do not override.
    symbolsByTree.putIfAbsent(identifier, symbol);
  }

  @Override
  @CheckForNull
  public Symbol getSymbol(Tree tree) {
    return symbolsByTree.get(tree);
  }

  public Collection<ClassSymbolData> classSymbolDatas() {
    return classSymbolData;
  }

  public Collection<FunctionSymbolData> functionSymbolDatas() {
    return functionSymbolData;
  }
}
