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
