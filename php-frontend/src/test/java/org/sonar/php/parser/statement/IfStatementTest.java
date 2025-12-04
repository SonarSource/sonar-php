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
package org.sonar.php.parser.statement;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class IfStatementTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.IF_STATEMENT)
      .matches("if ($a) {}")
      .matches("if ($a) {} elseif ($a) {}")
      .matches("if ($a) {} elseif ($a) {} elseif ($a) {}")
      .matches("if ($a) {} elseif ($a) {} else {}")
      .matches("if ($a) {} else {}")

      .matches("if ($a) : endif;")
      .matches("if ($a) : elseif ($a): endif;")
      .matches("if ($a) : elseif ($a): else: endif;")
      .matches("if ($a) : else: endif;")

      .notMatches("if ($a) : {}");
  }

  @Test
  void realLife() {
    assertThat(PHPLexicalGrammar.IF_STATEMENT)
      .matches("if (\"#$a\") {\n $x = ''; }");
  }

}
