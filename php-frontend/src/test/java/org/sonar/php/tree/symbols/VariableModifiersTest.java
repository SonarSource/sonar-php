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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.symbols.Symbol;

import static org.fest.assertions.Assertions.assertThat;

class VariableModifiersTest extends ParsingTestUtils {

  private SymbolTableImpl SYMBOL_TABLE = SymbolTableImpl.create(parse("symbols/variable_modifiers.php"));

  @Test
  void modifiers() {
    List<Symbol> symbols = SYMBOL_TABLE.getSymbols("$a");
    // FIXME SONARPHP-741: should be 3
    // line 8 should be an usage of global variable, and not generate a new symbol
    assertThat(symbols).hasSize(4);
  }

}
