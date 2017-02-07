/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import org.junit.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.symbols.Symbol;

import static org.assertj.core.api.Assertions.assertThat;

public class SymbolTableImplTest extends ParsingTestUtils {

  private SymbolTableImpl SYMBOL_MODEL = SymbolTableImpl.create(parse("symbols/symbolTable.php"));

  @Test
  public void symbols_filtering() {
    assertThat(SYMBOL_MODEL.getSymbols()).hasSize(16);

    assertThat(SYMBOL_MODEL.getSymbols(Symbol.Kind.FUNCTION)).hasSize(2);
    assertThat(SYMBOL_MODEL.getSymbols(Symbol.Kind.CLASS)).hasSize(1);
    assertThat(SYMBOL_MODEL.getSymbols(Symbol.Kind.FIELD)).hasSize(3);
    assertThat(SYMBOL_MODEL.getSymbols(Symbol.Kind.PARAMETER)).hasSize(1);
    assertThat(SYMBOL_MODEL.getSymbols(Symbol.Kind.VARIABLE)).hasSize(9);

    assertThat(SYMBOL_MODEL.getSymbols("$a")).hasSize(2);
    // Case insensitive
    assertThat(SYMBOL_MODEL.getSymbols("$A")).hasSize(2);
  }

  @Test
  public void test_class_fields() throws Exception {
    Symbol field = SYMBOL_MODEL.getSymbols("$field1").get(0);
    Symbol constantField = SYMBOL_MODEL.getSymbols("CONSTANT_FIELD").get(0);

    assertThat(field.hasModifier("public")).isTrue();
    assertThat(field.is(Symbol.Kind.FIELD)).isTrue();

    assertThat(constantField.hasModifier("const")).isTrue();
    assertThat(constantField.is(Symbol.Kind.FIELD)).isTrue();
  }

  @Test
  public void test_global_constant() throws Exception {
    Symbol constant = SYMBOL_MODEL.getSymbols("CONSTANT").get(0);

    assertThat(constant.hasModifier("const")).isTrue();
    assertThat(constant.is(Symbol.Kind.VARIABLE)).isTrue();
  }

  @Test
  public void list_variable() throws Exception {
    assertThat(SYMBOL_MODEL.getSymbols("$l1")).hasSize(1);
    assertThat(SYMBOL_MODEL.getSymbols("$l2")).hasSize(1);
  }

  @Test
  public void foreach_variable() throws Exception {
    assertThat(SYMBOL_MODEL.getSymbols("$key")).hasSize(1);
    assertThat(SYMBOL_MODEL.getSymbols("$val")).hasSize(1);
  }

  @Test
  public void static_variable() throws Exception {
    assertThat(SYMBOL_MODEL.getSymbols("$static")).hasSize(1);
  }

}
