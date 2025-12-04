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
package org.sonar.php.parser.expression;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class CastTypeTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.CAST_TYPE)
      .notMatches("(array)")
      .matches("(array)$a")
      .matches("(Array)$a")
      .matches("(ARRAY)$a")
      .notMatches("(unknown)$a")
      .matches("(unset)$a")
      .matches("(integer)$a")
      .matches("(int)$a")
      .matches("(double)$a")
      .matches("(float)$a")
      .matches("(real)$a")
      .matches("(string)$a")
      .matches("(object)$a")
      .matches("(boolean)$a")
      .matches("(bool)$a")
      .matches("(binary)$a")
      .matches("(binary)'hello'")
      .matches("(binary)\"hello\"")
      .matches("(binary)<<<EOT\nhello\nEOT")
      .matches("(binary)<<<\"EOT\"\nhello\nEOT")
      .matches("(binary)<<<'EOT'\nhello\nEOT")
      .matches("b'hello'")
      .notMatches("b")
      .notMatches("c'hello'")
      .notMatches("'hello'")
      .matches("B\"hello\"")
      .matches("b<<<EOT\nhello\nEOT")
      .matches("b<<<\"EOT\"\nhello\nEOT")
      .matches("b<<<'EOT'\nhello\nEOT")
      .notMatches("<<<EOT\nhello\nEOT");
  }

}
