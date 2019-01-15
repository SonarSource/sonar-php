/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.php.checks.utils.type;

import org.junit.Test;
import org.sonar.php.tree.symbols.SymbolTableImpl;
import org.sonar.plugins.php.api.symbols.SymbolTable;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.php.checks.utils.type.TreeValuesTest.expression;

public class NewObjectCallTest {

  @Test
  public void test() {
    CompilationUnitTree unit = (CompilationUnitTree) TreeValuesTest.PARSER.parse("<?php " +
      /* expr0 */ "new A();" +
      /* expr1 */ "new $name();" +
      /* expr2 */ "new class{};" +
      /* expr3 */ "NULL;");
    SymbolTable symbolTable = SymbolTableImpl.create(unit);
    TreeValues expr0 = TreeValues.of(expression(unit, 0), symbolTable);
    TreeValues expr1 = TreeValues.of(expression(unit, 1), symbolTable);
    TreeValues expr2 = TreeValues.of(expression(unit, 2), symbolTable);
    TreeValues expr3 = TreeValues.of(expression(unit, 3), symbolTable);

    assertThat(new NewObjectCall("A").test(expr0)).isTrue();
    assertThat(new NewObjectCall("a").test(expr0)).isTrue();
    assertThat(new NewObjectCall("B").test(expr0)).isFalse();
    assertThat(new NewObjectCall("A").test(expr1)).isFalse();
    assertThat(new NewObjectCall("A").test(expr2)).isFalse();
    assertThat(new NewObjectCall("A").test(expr3)).isFalse();
  }

}
