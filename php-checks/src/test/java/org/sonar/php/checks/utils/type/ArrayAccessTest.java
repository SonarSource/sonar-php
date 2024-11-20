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
package org.sonar.php.checks.utils.type;

import org.junit.jupiter.api.Test;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.statement.ForEachStatementTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.checks.utils.type.TreeValuesTest.expression;

class ArrayAccessTest {

  @Test
  void test() {
    CompilationUnitTree unit = (CompilationUnitTree) TreeValuesTest.PARSER.parse("<?php " +
    /* expr0 */ "$x[0];" +
    /* expr1 */ "$y;" +
    /* expr2 */ "foreach ($arr as $value) { }" +
    /* expr3 */ "foreach ($arr as $key => $value) { }");
    SymbolTable symbolTable = SymbolTableImpl.create(unit);
    TreeValues expr0 = TreeValues.of(expression(unit, 0), symbolTable);
    TreeValues expr1 = TreeValues.of(expression(unit, 1), symbolTable);
    ForEachStatementTree expr2 = (ForEachStatementTree) unit.script().statements().get(2);
    ForEachStatementTree expr3 = (ForEachStatementTree) unit.script().statements().get(3);

    TreeValues expr2value = TreeValues.of(expr2.value(), symbolTable);
    TreeValues expr2expression = TreeValues.of(expr2.expression(), symbolTable);
    TreeValues expr3value = TreeValues.of(expr3.value(), symbolTable);

    assertThat(new ArrayAccess(values -> true).test(expr0)).isTrue();
    assertThat(new ArrayAccess(values -> false).test(expr0)).isFalse();
    assertThat(new ArrayAccess(values -> true).test(expr1)).isFalse();
    assertThat(new ArrayAccess(values -> true).test(expr2value)).isTrue();
    assertThat(new ArrayAccess(values -> true).test(expr2expression)).isFalse();
    assertThat(new ArrayAccess(values -> values.values.get(0) == expr2.expression()).test(expr2value)).isTrue();
    assertThat(new ArrayAccess(values -> values.values.get(0) == expr3.expression()).test(expr2value)).isFalse();
    assertThat(new ArrayAccess(values -> values.values.get(0) == expr3.expression()).test(expr3value)).isTrue();
  }

}
