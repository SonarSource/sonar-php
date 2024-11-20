/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.tree.symbols;

import org.sonar.plugins.php.api.symbols.QualifiedName;

/**
 * {@link UndeclaredSymbol} is used for class symbols which do not have declaration available (built-in classes or classes declared in
 * another compilation unit
 */
class UndeclaredSymbol extends SymbolImpl {

  UndeclaredSymbol(QualifiedName qualifiedName, Kind kind) {
    super(qualifiedName, kind);
  }
}
