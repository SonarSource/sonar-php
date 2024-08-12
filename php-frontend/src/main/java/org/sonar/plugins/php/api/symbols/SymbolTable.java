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
package org.sonar.plugins.php.api.symbols;

import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.php.tree.symbols.Scope;
import org.sonar.plugins.php.api.tree.Tree;

public interface SymbolTable {

  /**
   *
   * @param kind kind of symbols to look for
   * @return list of symbols with the given kind
   */
  List<Symbol> getSymbols(Symbol.Kind kind);

  Set<Scope> getScopes();

  @Nullable
  Scope getScopeFor(Tree tree);

  Symbol getSymbol(Tree tree);

  Framework getFramework();

  /**
   * Some specific PHP frameworks can be detected by the plugin. The rules can then use this information
   * to adapt their behavior.
   */
  enum Framework {
    DRUPAL,
    EMPTY,
  }
}
