/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.php.parser.declaration;

import org.junit.jupiter.api.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

class DnfTypeTest {

  @Test
  void shouldParse() {
    assertThat(PHPLexicalGrammar.DNF_TYPE)
      .matches("(A&B)")
      .matches("(A&B)|int")
      .matches("int|(A&B)")
      .matches("int|null|(A&B)")
      .matches("int|(A&B)|null")
      .matches("(A&B)|(C&D)")
      .matches("(A&B)|C")

      .notMatches("int|null")
      .notMatches("int")
      .notMatches("A&B")
      .notMatches("A&B|null")
      .notMatches("A&null")
      .notMatches("null");
  }
}
