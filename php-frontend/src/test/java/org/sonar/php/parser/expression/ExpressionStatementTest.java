/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2026 SonarSource Sàrl
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

class ExpressionStatementTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.EXPRESSION_STATEMENT)
      .matches("1;")
      .matches("1 + 1;")
      .matches("yield $a;")
      .matches("yield +6;")
      .matches("yield *6;")
      .matches("foo(MATCH);")
      .matches("foo\\match();")
      .matches("match($a);")
      .matches("match($a) {$b=>2};");
  }

}
