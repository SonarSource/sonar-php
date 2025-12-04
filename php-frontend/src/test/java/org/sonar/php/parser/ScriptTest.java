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
package org.sonar.php.parser;

import org.junit.jupiter.api.Test;

import static org.sonar.php.utils.Assertions.assertThat;

class ScriptTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.SCRIPT)
      .matches("<?php")
      .matches("<?php const A = 1; function foo(){}")

      .notMatches("\n")
      .notMatches("");
  }

  @Test
  void shouldParseExpressionListStatement() {
    assertThat(PHPLexicalGrammar.SCRIPT)
      .matches("<?= $x, $x + 1 ?> <tag> <?= $x*2; echo 42 ?>")

      // matches due our grammar permissiveness
      // parsing error in interpreter
      .matches("<?php $x ?>")
      .matches("<?php echo 42; $x ?>");
  }

}
