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
package org.sonar.php.tree.symbols;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.CheckForNull;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SymbolTableImpl implements SymbolTable {

  private List<Symbol> symbols = new ArrayList<>();
  private Set<Scope> scopes = Sets.newHashSet();
  private Map<Tree, Symbol> symbolsByTree = new HashMap<>();

  private SymbolTableImpl(){
  }

  public static SymbolTableImpl create(CompilationUnitTree compilationUnit) {
    SymbolTableImpl symbolModel = new SymbolTableImpl();
    new SymbolVisitor(symbolModel).visitCompilationUnit(compilationUnit);
    return symbolModel;
  }

  public void addScope(Scope scope){
    scopes.add(scope);
  }

  @Override
  public ImmutableSet<Scope> getScopes(){
    return ImmutableSet.copyOf(scopes);
  }

  @Nullable
  @Override
  public Scope getScopeFor(Tree tree) {
    for (Scope scope : scopes) {
      if (scope.tree().equals(tree)) {
        return scope;
      }
    }
    return null;
  }

  public Symbol declareSymbol(IdentifierTree name, Symbol.Kind kind, Scope scope) {
    Symbol symbol = new Symbol(name, kind, scope);
    symbols.add(symbol);
    scope.addSymbol(symbol);
    return symbol;
  }

  /**
   * Returns all symbols in script
   */
  public ImmutableList<Symbol> getSymbols() {
    return ImmutableList.copyOf(symbols);
  }

  /**
   *
   * @param kind kind of symbols to look for
   * @return list of symbols with the given kind
   */
  @Override
  public List<Symbol> getSymbols(Symbol.Kind kind) {
    List<Symbol> result = new ArrayList<>();
    for (Symbol symbol : getSymbols()){
      if (kind.equals(symbol.kind())){
        result.add(symbol);
      }
    }
    return result;
  }

  /**
   *
   * @param name name of symbols to look for
   * @return list of symbols with the given name
   */
  public List<Symbol> getSymbols(String name) {
    List<Symbol> result = new ArrayList<>();
    for (Symbol symbol : getSymbols()){
      if (symbol.called(name)) {
        result.add(symbol);
      }
    }
    return result;
  }

  void associateSymbol(Tree identifier, Symbol symbol) {
    symbolsByTree.put(identifier, symbol);
  }


  @Override
  @CheckForNull
  public Symbol getSymbol(Tree tree) {
    return symbolsByTree.get(tree);
  }
}
