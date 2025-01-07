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

class VariableDeclarationTest {

  @Test
  void shouldParseVariableDeclarations() {
    assertThat(PHPLexicalGrammar.VARIABLE_DECLARATION)
      .matches("$a")
      .matches("$a = 1")
      .matches("$a = 1")
      .matches("$a = fn($x) => $x * 5")
      .matches("$a = (fn($x) => $x * 5)(7)")
      .matches("$a = array(1, 2, 3)")
      .matches("$a = []")
      .matches("$a = [1]")
      .matches("$a = [[1], [2]]")
      .matches("$a = [\"bar\" => 3][\"bar\"]")
      .matches("$a = [][][$a][$a or $b]")
      .matches("$a = [1, 2, $b][]")
      .matches("$a = [1] + [1, 2]")

      .notMatches("$a = [1, 2, 3][$a, $b]");
  }

}
