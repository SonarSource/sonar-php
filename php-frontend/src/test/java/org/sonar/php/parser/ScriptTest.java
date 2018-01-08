/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.parser;

import org.junit.Test;

import static org.sonar.php.utils.Assertions.assertThat;

public class ScriptTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.SCRIPT)
      .matches("<?php")
      .matches("<?php const A = 1; function foo(){}")

      .notMatches("\n")
      .notMatches("")
    ;
  }

  @Test
  public void should_parse_expression_list_statement() throws Exception {
    assertThat(PHPLexicalGrammar.SCRIPT)
      .matches("<?= $x, $x + 1 ?> <tag> <?= $x*2; echo 42 ?>")

      // matches due our grammar permissiveness
      // parsing error in interpreter
      .matches("<?php $x ?>")
      .matches("<?php echo 42; $x ?>")
    ;
  }
}
