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
package org.sonar.php.parser.statement;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class EnumCaseTest {

  @Test
  void shouldParseEnumCases() {
    assertThat(PHPLexicalGrammar.ENUM_CASE)
      .matches("case A;")
      .matches("#[A1(1)] case A;")
      .matches("case Enum;")
      .matches("case A = 'A';")
      .matches("case A = 'A' . 'B';")
      .matches("case A = MyClass::CONSTANT;")
      .matches("case NEW = 1;")
      .matches("case CLASS = 'a';")
      .matches("case EVAL = 'c';")
      .matches("case ARR = array(1, 2, 3);")
      .matches("case ARR = [];")
      .matches("case ARR = [1];")
      .matches("case ARR = [[1], [2]];")
      .matches("case ARR = [\"bar\" => 3][\"bar\"];")
      .matches("case ARR = [][][$a][$a or $b];")
      .matches("case ARR = [1, 2, $b][];")
      .matches("case ARR = [1] + [1, 2];")

      .notMatches("case A")
      .notMatches("case ARR = [1, 2, 3][$b, $c];");
  }
}
