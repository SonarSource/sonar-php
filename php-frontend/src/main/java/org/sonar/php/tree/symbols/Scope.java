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

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.Symbol.Kind;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;

public class Scope {

  private final Scope outer;
  private final Tree tree;
  protected List<Symbol> symbols = new ArrayList<>();
  Scope superClassScope;
  private boolean unresolvedCompact;
  private boolean captureOuterScope;

  public Scope(Scope outer, Tree tree, boolean captureOuterScope) {
    this.outer = Preconditions.checkNotNull(outer);
    this.tree = Preconditions.checkNotNull(tree);
    this.captureOuterScope = captureOuterScope;
  }

  /**
   * Used for global scope
   *
   */
  public Scope(CompilationUnitTree compilationUnitTree) {
    this.outer = null;
    this.tree = compilationUnitTree;
    this.captureOuterScope = false;
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

  /**
   * returns symbol available in this scope with satisfying name and kind.
   * If no or more than one symbols meet conditions, then null is returned.
   */
  @Nullable
  public Symbol getSymbol(String name, Kind... kinds) {
    List<Kind> kindList = Arrays.asList(kinds);
    List<Symbol> result = new ArrayList<>();
    for (Symbol s : symbols) {
      if (s.called(name)) {
        if (kindList.isEmpty() || kindList.contains(s.kind())) {
          result.add(s);
        } else if (s.is(Kind.PARAMETER) && kindList.equals(Collections.singletonList(Kind.VARIABLE))) {
          // parameter of a child scope shadows variable of the outer scope
          return null;
        }
      }
    }
    if (result.isEmpty()) {
      if (superClassScope != null) {
        return superClassScope.getSymbol(name, kinds);
      } else if (captureOuterScope) {
        return outer.getSymbol(name, kinds);
      }
    }

    return result.size() == 1 ? result.get(0) : null;
  }

  void setUnresolvedCompact(boolean unresolvedCompact) {
    this.unresolvedCompact = unresolvedCompact;
  }

  public boolean hasUnresolvedCompact() {
    return unresolvedCompact;
  }
}
