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
package org.sonar.php.parser.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class ArrayInitializerTest {

  @Test
  void shouldParseArrayInitializers() {
    assertThat(PHPLexicalGrammar.ARRAY_INITIALIZER)
      .matches("array()")
      .matches("array(1, 2, 3)")
      .matches("array('key' => 'value', 'key2' => 'value2')")
      .matches("array($a, getArr(), &$a, $a=>$b, $a=>&$b, ...$a, ...getArr())")
      .matches("array(array(), [])")

      .matches("[]")
      .matches("[1, 2, 3]")
      .matches("['key' => 'value', 'key2' => 'value2']")
      .matches("[$a, getArr(), &$a, $a=>$b, $a=>&$b, ...$a, ...getArr()]")
      .matches("[[], array()]")

      .matches("""
        [
            ["\\xc3\\x28", '"\\ufffd("', 'invalid unicode sequence'],
            [
                [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[33]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]],
                'exception',
                'Maximum stack depth exceeded'
            ]
        ]""")

      .notMatches("array(1, 2, 3")
      .notMatches("[1, 2, 3")
      .notMatches("[][]");
  }

}
