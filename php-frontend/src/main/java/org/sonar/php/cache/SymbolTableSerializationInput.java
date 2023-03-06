/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.php.cache;

import org.sonar.php.tree.symbols.SymbolTableImpl;

public class SymbolTableSerializationInput {
  private final SymbolTableImpl symbolTable;
  private final String pluginVersion;

  public SymbolTableSerializationInput(SymbolTableImpl symbolTable, String pluginVersion) {
    this.symbolTable = symbolTable;
    this.pluginVersion = pluginVersion;
  }

  public SymbolTableImpl symbolTable() {
    return symbolTable;
  }

  public String pluginVersion() {
    return pluginVersion;
  }
}
