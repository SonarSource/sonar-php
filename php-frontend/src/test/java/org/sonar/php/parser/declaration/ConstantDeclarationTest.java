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
package org.sonar.php.parser.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class ConstantDeclarationTest {

  @Test
  void shouldParseConstantDeclarations() {
    assertThat(PHPLexicalGrammar.CONSTANT_DECLARATION)
      .matches("const A = 1 ;")
      .matches("const A = 1, B = 2 ;")
      .matches("const MATCH = 2;")
      .matches("const ARR = [];")
      .matches("const ARR = [1];")
      .matches("const ARR = [[1], [2]];")
      .matches("const ARR = [\"bar\" => 3][\"bar\"];")
      .matches("const ARR = [][][$a][$a or $b];")
      .matches("const ARR = [1, 2, $b][];")
      .matches("const ARR = [1] + [1, 2];")

      .notMatches("public const A = 1 ;")
      .notMatches("const A ;")
      .notMatches("const ARR = [1, 2, 3][$a, $b];");
  }
}
