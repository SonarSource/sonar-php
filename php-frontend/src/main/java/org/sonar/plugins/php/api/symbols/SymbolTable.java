/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.api.symbols;

import java.util.List;
import java.util.Set;
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
    YII,
    LARAVEL,
    WORDPRESS,
  }
}
