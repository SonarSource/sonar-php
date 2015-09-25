/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010 SonarSource and Akram Ben Aissi
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.php.parser.declaration;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

// fixme (Lena) : this test should be refactored (it tests different grammar rules but one Tree interface)
public class VariableDeclarationTest {

  @Test
  public void member_const_declaration() {
    assertThat(PHPLexicalGrammar.MEMBER_CONST_DECLARATION)
      .matches("a")
      .matches("a = $a");
  }

  @Test
  public void static_var() {
    assertThat(PHPLexicalGrammar.STATIC_VAR)
      .matches("$a")
      .matches("$a = $a");
  }

  @Test
  public void variable_declaration() {
    // todo (Lena) : uncomment when VARIABLE_DECLARATION is defined
//    assertThat(PHPLexicalGrammar.VARIABLE_DECLARATION)
//        .matches("$a")
//        .matches("$a = 1");
  }


}
