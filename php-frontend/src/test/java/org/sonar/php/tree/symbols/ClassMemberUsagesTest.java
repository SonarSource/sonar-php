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
package org.sonar.php.tree.symbols;

import org.junit.jupiter.api.Test;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.Symbol.Kind;

import static org.assertj.core.api.Assertions.assertThat;

class ClassMemberUsagesTest extends ParsingTestUtils {

  private final SymbolTableImpl SYMBOL_TABLE = SymbolTableImpl.create(parse("symbols/class_members_usages.php"));

  @Test
  void testStaticField() {
    Symbol staticField = getSymbol("$staticField", Kind.FIELD);
    assertThat(staticField).isNotNull();
    assertThat(staticField.usages()).hasSize(4);
  }

  @Test
  void testField() {
    Symbol field = getSymbol("$field", Kind.FIELD);
    assertThat(field).isNotNull();
    assertThat(field.usages()).hasSize(2);

    Symbol arrayField = getSymbol("$fieldArray", Kind.FIELD);
    assertThat(arrayField).isNotNull();
    assertThat(arrayField.usages()).hasSize(1);
  }

  @Test
  void testMethod() {
    Symbol method = getSymbol("method", Kind.FUNCTION);
    Symbol field = getSymbol("$method", Kind.FIELD);

    assertThat(method).isNotNull();
    assertThat(field).isNotNull();

    assertThat(field.usages()).hasSize(1);
    assertThat(method.usages()).hasSize(7);
  }

  @Test
  void testStaticMethod() {
    Symbol method = getSymbol("staticMethod", Kind.FUNCTION);
    Symbol field = getSymbol("$staticMethod", Kind.FIELD);

    assertThat(method).isNotNull();
    assertThat(field).isNotNull();

    assertThat(field.usages()).hasSize(1);
    assertThat(method.usages()).hasSize(5);
  }

  @Test
  void testConstField() {
    Symbol constField = getSymbol("constField", Kind.FIELD);
    Symbol field = getSymbol("$constField", Kind.FIELD);

    assertThat(constField).isNotNull();
    assertThat(field).isNotNull();

    assertThat(constField.usages()).hasSize(1);
    assertThat(field.usages()).hasSize(1);
  }

  @Test
  void testUsedBeforeDeclaration() {
    Symbol method = getSymbol("lateDeclMethod", Kind.FUNCTION);
    Symbol field = getSymbol("$lateDeclField", Kind.FIELD);

    assertThat(method).isNotNull();
    assertThat(field).isNotNull();

    assertThat(field.usages()).hasSize(1);
    assertThat(method.usages()).hasSize(1);
  }

  @Test
  void testPropertyNameInVariable() {
    Symbol variable = getSymbol("$a", Kind.VARIABLE);
    Symbol parameter = getSymbol("$p", Kind.PARAMETER);

    assertThat(variable).isNotNull();
    assertThat(parameter).isNotNull();

    assertThat(variable.usages()).hasSize(1);
    assertThat(parameter.usages()).hasSize(1);

  }

  @Test
  void testLocalVarAsMembers() {
    Symbol variable = getSymbol("$fieldN", Kind.VARIABLE);
    Symbol parameter = getSymbol("$funcN", Kind.PARAMETER);

    Symbol field = getSymbol("$fieldToTestVariables", Kind.FIELD);
    Symbol method = getSymbol("someStaticMethod", Kind.FUNCTION);

    assertThat(variable).isNotNull();
    assertThat(parameter).isNotNull();
    assertThat(field).isNotNull();
    assertThat(method).isNotNull();

    assertThat(variable.usages()).hasSize(1);
    assertThat(parameter.usages()).hasSize(1);

    // actually these method and field are used but we do not support this use case
    assertThat(field.usages()).isEmpty();
    assertThat(method.usages()).isEmpty();

  }

  @Test
  void testInheritanceConstantLookup() {
    Symbol variable = getSymbol("A_CONST", Kind.FIELD);
    assertThat(variable).isNotNull();
    assertThat(variable.usages()).hasSize(1);
  }

  private Symbol getSymbol(String name, Kind kind) {
    Symbol result = null;
    for (Symbol symbol : SYMBOL_TABLE.getSymbols(name)) {
      if (symbol.is(kind) && result == null) {
        result = symbol;
      } else {
        return null;
      }
    }
    return result;
  }
}
