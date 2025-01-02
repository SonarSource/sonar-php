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

class ForStatementTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.FOR_STATEMENT)
      .matches("for ($i = 1; $i <= 10; $i++) {}")
      .matches("for ($a1 = 1, $a2 = 2;  $b1 <= 10, $b2 > 5;  $c1++, $c2--) {}")
      .matches("for ($a; ; $c) {}")
      .matches("for (; ; ) {}")
      .matches("for (; ; ): {} {} endfor;")
      .matches("for (; ; ): {} endfor;")
      .matches("for (; ; ): endfor;");
  }
}
