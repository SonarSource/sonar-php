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
package org.sonar.php.parser.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class AssignmentExpressionTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.ASSIGNMENT_EXPRESSION)
      .matches("$a = $b")
      .matches("$a **= $b")
      .matches("$a *= $b")
      .matches("$a /= $b")
      .matches("$a %= $b")
      .matches("$a += $b")
      .matches("$a -= $b")
      .matches("$a <<= $b")
      .matches("$a >>= $b")
      .matches("$a &= $b")
      .matches("$a ^= $b")
      .matches("$a |= $b")
      .matches("$a -= $b")
      .matches("$a -= $b")

      .matches("$a =& $b")
      .matches("$a =& new X")
      .matches("$a =& myFunction()")

      .matches("$array = [1, 2]")
      .matches("$array = [1, 2, 3, [3, 4]]")
      .matches("$a = ['one' => 1, 'two' => 2]")
      .matches("[$a, $b] = $array")
      .matches("list($a, $b) = $array")
      .matches("[$a, &$b] = $array")
      .matches("list($a, &$b) = $array")
      .matches("$array = [1, 2, 3, [3, 4]]")
      .matches("$bar = [\"bar\" => 3][\"bar\"]")

      .notMatches("[$a, &&$b] = $array")
      .notMatches("[$a, &] = $array")
      .notMatches("[] = $array")
      .notMatches("list($a, &&$b) = $array")
      .notMatches("list($a, &) = $array")

      .notMatches("$a =& $b * $c")

      .matches("$var = function () {}")
      .matches("$a = $b = 1")
      .matches("$a ??= $b")
      .matches("$a ??= myFunction()");
  }
}
