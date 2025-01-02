/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.parser.statement;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class StatementTest {

  @Test
  void test() {
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
      .matches("$var = function () {};")
      .matches("foo();")
      .matches("Foo::bar();")
      .matches("'Foo::bar'();")
      .matches("['Foo','bar']();")
      .matches("[A::class, $method_name]();")
      .matches("null();");
  }

  @Test
  void optionalSemicolon() {
    assertThat(PHPLexicalGrammar.STATEMENT)
      .matches("continue ?>");
  }

  @Test
  void topStatement() {
    assertThat(PHPLexicalGrammar.TOP_STATEMENT)
      .matches("__halt_compiler();");
  }

}
