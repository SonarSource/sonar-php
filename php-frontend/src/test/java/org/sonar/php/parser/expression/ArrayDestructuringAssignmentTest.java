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

class ArrayDestructuringAssignmentTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.ARRAY_DESTRUCTURING_ASSIGNMENT)
      .matches("[$a, $b] = $array")
      .matches("[$a, &$b] = $array")
      .matches("[&$a, $b,, [&$c, $d]] = $array")
      .matches("[&$one, [$two, &$three]] = $a")
      .matches("['one' => &$one, 'two' => $two] = $a")
      .notMatches("$array = [1, 2]")
      .notMatches("list($a, &$b) = $array");
  }

}
