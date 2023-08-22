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
package org.sonar.php.tree.visitors;

import java.util.Optional;
import org.junit.jupiter.api.Test;
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

  @Test
  void getAssignmentValue() {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.of("$a").from("<?php function foo() { $a = 1; }");

    assertThat(uniqueAssignedValue).isPresent();
    ExpressionTree value = uniqueAssignedValue.get();
    assertThat(value).isInstanceOf(LiteralTree.class);
    assertThat(((LiteralTree) value).value()).isEqualTo("1");
  }

  @Test
  void getAssignmentValueGlobal() {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.of("$a").from("<?php $a = 1;");

    assertThat(uniqueAssignedValue).isPresent();
    ExpressionTree value = uniqueAssignedValue.get();
    assertThat(value).isInstanceOf(LiteralTree.class);
    assertThat(((LiteralTree) value).value()).isEqualTo("1");
  }

  @Test
  void getAssignmentValueMultiple() {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.of("$a").from("<?php $a = 1;\n$a = 2;");

    assertThat(uniqueAssignedValue).isNotPresent();
  }

  @Test
  void getAssignmentValueList() {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.of("$a").from("<?php list($a, $b) = [1, 2];");

    assertThat(uniqueAssignedValue).isPresent();
    ExpressionTree value = uniqueAssignedValue.get();
    assertThat(value).isInstanceOf(LiteralTree.class);
    assertThat(((LiteralTree) value).value()).isEqualTo("1");
  }

  @Test
  void getAssignmentValueListUnknownValues() {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.of("$a").from("<?php $a = 1; list($a, $b) = getValues();");

    assertThat(uniqueAssignedValue).isNotPresent();
  }

  @Test
  void getAssignmentValueListSkippedElement() {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.of("$b").from("<?php list($a, , $b) = [1, 2, 3];");

    assertThat(uniqueAssignedValue).isPresent();
    ExpressionTree value = uniqueAssignedValue.get();
    assertThat(value).isInstanceOf(LiteralTree.class);
    assertThat(((LiteralTree) value).value()).isEqualTo("3");
  }

  @Test
  void getAssignmentValueListVarKeysNotSupportedYet() {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.of("$a").from("<?php list(getAKey() => $a) = ['a'];");

    assertThat(uniqueAssignedValue).isNotPresent();
  }

  @Test
  void getAssignmentValueListValueKeysNotSupportedYet() {
    Optional<ExpressionTree> uniqueAssignedValue = UniqueAssignedValue.of("$a").from("<?php list($a, $b) = [getAKey() => 'a', getBKey() => 'b'];");

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
