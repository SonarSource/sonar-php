/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import java.util.List;
import org.junit.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.symbols.Symbol;

import static org.fest.assertions.Assertions.assertThat;

public class VariableModifiersTest extends ParsingTestUtils {

  private SymbolTableImpl SYMBOL_TABLE = SymbolTableImpl.create(parse("symbols/variable_modifiers.php"));

  @Test
  public void modifiers() {
    List<Symbol> symbols = SYMBOL_TABLE.getSymbols("$a");
    // FIXME SONARPHP-741: should be 3
    // line 8 should be an usage of global variable, and not generate a new symbol
    assertThat(symbols).hasSize(4);
  }

}
