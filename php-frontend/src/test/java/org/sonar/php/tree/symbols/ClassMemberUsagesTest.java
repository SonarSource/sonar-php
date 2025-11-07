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
package org.sonar.php.tree.symbols;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.php.ParsingTestUtils;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.Symbol.Kind;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ClassMemberUsagesTest extends ParsingTestUtils {

  @RegisterExtension
  public final LogTesterJUnit5 logTester = new LogTesterJUnit5();

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

  @ParameterizedTest
  @MethodSource
  void testMethod(String symbolName, String fieldName, int expectedMethodUsages) {
    Symbol method = getSymbol(symbolName, Kind.FUNCTION);
    Symbol field = getSymbol(fieldName, Kind.FIELD);

    assertThat(method).isNotNull();
    assertThat(field).isNotNull();

    assertThat(field.usages()).hasSize(1);
    assertThat(method.usages()).hasSize(expectedMethodUsages);
  }

  private static Stream<Arguments> testMethod() {
    return Stream.of(
      Arguments.of("method", "$method", 7),
      Arguments.of("staticMethod", "$staticMethod", 5),
      Arguments.of("lateDeclMethod", "$lateDeclField", 1));
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

  @Test
  void shouldNotOverflowBuildingSymbolTableForLongChain() {
    var code = """
      <?php
      class A {
        function foo() {
          return $this;
        }
      }
      (new A())->foo()
      """ + "\n->foo()".repeat(1500) +
      ";";

    assertThatCode(() -> SymbolTableImpl.create(parseSource(code)))
      .doesNotThrowAnyException();
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
