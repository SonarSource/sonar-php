/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
