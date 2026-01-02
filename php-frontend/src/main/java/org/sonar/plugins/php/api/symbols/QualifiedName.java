/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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

import org.sonar.php.tree.symbols.SymbolQualifiedName;

/**
 * Represents fully qualified name of the symbol, like {@code namespace\foo\bar }. Use {@link #toString()} to get String representation
 * All qualified names are normalized to lowercase, because PHP is case-insensitive
 */
public interface QualifiedName {

  /**
   * @return the last element of qualified name i.e. {@code bar} for {@code namespace\foo\bar} , or {@code method} for {@code namespace\A::method}
   *
   */
  String simpleName();

  static QualifiedName qualifiedName(String qualifiedNameString) {
    return SymbolQualifiedName.qualifiedName(qualifiedNameString);
  }

}
