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
package org.sonar.php.parser.statement;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

public class StatementTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.STATEMENT)
    .matches("{}")
    .matches("label:")
    .matches("if ($a): endif;")
    .matches("while($a) {}")
    .matches("for ($i = 1; $i <= 10; $i++) {}")
    .matches("switch ($a) {}")
    .matches("break;")
    .matches("continue;")
    .matches("return;")
    .matches(";")
    .matches("yield $a;")
    .matches("[$a, &$b] = $array;")
    .matches("list($a, &$b) = $array;")
    .matches("foreach ($array as list(&$a, $b)) { $a = 7; }")
    .matches("global $a;")
    .matches("echo \"Hi\";")
    .matches("$a = b'hello';")
    .matches("unset($a);")
    .matches("yield yield;")
    .matches("die(yield $foo);")
    .matches("yield from [yield];")
    .matches("list($value) = yield;")
    .matches("var_dump(yield * -1);")
    .matches("var_dump([yield \"k\" => \"a\" . \"b\"]);")
    .matches("$$varName = yield;")
    .matches("$gen = yield;")
    .matches("$var = function () {};");
  }

  @Test
  public void optional_semicolon() {
    assertThat(PHPLexicalGrammar.STATEMENT)
      .matches("continue ?>");
  }

  @Test
  public void top_statement() {
    assertThat(PHPLexicalGrammar.TOP_STATEMENT)
      .matches("__halt_compiler();");
  }

}
