/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.tree.symbols;

import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.tree.Tree;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Scope {

  private Scope outer;
  private final Tree tree;
  protected List<Symbol> symbols = new ArrayList<>();

  public Scope(Scope outer, Tree tree) {
    this.outer = outer;
    this.tree = tree;
  }

  public Tree tree() {
    return tree;
  }

  public Scope outer() {
    return outer;
  }

  /**
   * @param kind of the symbols to look for
   * @return the symbols corresponding to the given kind
   */
  public List<Symbol> getSymbols(Symbol.Kind kind) {
    List<Symbol> result = new LinkedList<>();
    for (Symbol symbol : symbols) {
      if (symbol.is(kind)) {
        result.add(symbol);
      }
    }
    return result;
  }

  public boolean isGlobal() {
    return tree.is(Tree.Kind.COMPILATION_UNIT);
  }

  public void addSymbol(Symbol symbol) {
    symbols.add(symbol);
  }

  @Nullable
  public Symbol getSymbol(String name, Symbol.Kind kind) {
    for (Symbol s : symbols) {
      if (s.called(name) && s.is(kind)) {
        return s;
      }
    }
    return null;
  }

  @Nullable
  public Symbol getSymbol(String name) {
    for (Symbol s : symbols) {
      if (s.called(name)) {
        return s;
      }
    }
    return null;
  }
}
