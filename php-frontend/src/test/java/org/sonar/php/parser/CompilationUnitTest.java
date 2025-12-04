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

class CompilationUnitTest {

  @Test
  void test() {
    assertThat(PHPLexicalGrammar.COMPILATION_UNIT)
      .matches("")

      .matches("<?php")

      .matches("html <?php")
      .matches("html <?")
      .matches("html <?=")

      .matches("<?php ?> html")
      .matches("<?php ?> html <?php")
      .matches("<?php ?> html <?php ?> html")

      // PHP closing tag ends comment
      .matches("<?php { // comment ?> <?php } ?>");
  }

}
