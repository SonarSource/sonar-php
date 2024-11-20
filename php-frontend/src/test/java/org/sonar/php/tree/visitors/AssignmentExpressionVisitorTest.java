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
package org.sonar.php.tree.visitors;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.tree.symbols.SymbolImpl;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.Symbol;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.IdentifierTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentExpressionVisitorTest {

  @ParameterizedTest
  @MethodSource
  void getAssignmentValue(String variableName, String codeSnippet, String expectedValue) {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.of(variableName).from(codeSnippet);

    assertThat(uniqueAssignedValue).isPresent();
    ExpressionTree value = uniqueAssignedValue.get();
    assertThat(value).isInstanceOf(LiteralTree.class);
    assertThat(((LiteralTree) value).value()).isEqualTo(expectedValue);
  }

  private static Stream<Arguments> getAssignmentValue() {
    return Stream.of(
      Arguments.of("$a", "<?php function foo() { $a = 1; }", "1"),
      Arguments.of("$a", "<?php $a = 1;", "1"),
      Arguments.of("$a", "<?php list($a, $b) = [1, 2];", "1"),
      Arguments.of("$b", "<?php list($a, , $b) = [1, 2, 3];", "3"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "<?php $a = 1;\n$a = 2;",
    "<?php $a = 1; list($a, $b) = getValues();",
    "<?php list(getAKey() => $a) = ['a'];",
    "<?php list($a, $b) = [getAKey() => 'a', getBKey() => 'b'];"})
  void getAssignmentValueMultiple(String codeSnippet) {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.of("$a").from(codeSnippet);

    assertThat(uniqueAssignedValue).isNotPresent();
  }

  private static class UniqueAssignedValue extends PHPTreeModelTest {
    private String name;

    UniqueAssignedValue(String name) {
      this.name = name;
    }

    static UniqueAssignedValue of(String name) {
      return new UniqueAssignedValue(name);
    }

    Optional<ExpressionTree> from(String code) {
      CompilationUnitTree tree = parse(code, PHPLexicalGrammar.COMPILATION_UNIT);
      SymbolTable symbolTable = SymbolTableImpl.create(tree);
      IdentifierTree var = ((SymbolTableImpl) symbolTable).getSymbols(name).get(0).declaration();
      Symbol symbol = symbolTable.getSymbol(var);
      if (symbol != null) {
        return ((SymbolImpl) symbol).uniqueAssignedValue();
      }
      return Optional.empty();
    }
  }

}
