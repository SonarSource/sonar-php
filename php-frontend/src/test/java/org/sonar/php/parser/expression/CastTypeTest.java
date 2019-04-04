/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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
package org.sonar.php.parser.expression;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

public class CastTypeTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.CAST_TYPE)
      .notMatches("(array)")
      .matches("(array)$a")
      .matches("(Array)$a")
      .matches("(ARRAY)$a")
      .notMatches("(unknown)$a")
      .matches("(unset)$a")
      .matches("(integer)$a")
      .matches("(int)$a")
      .matches("(double)$a")
      .matches("(float)$a")
      .matches("(real)$a")
      .matches("(string)$a")
      .matches("(object)$a")
      .matches("(boolean)$a")
      .matches("(bool)$a")
      .matches("(binary)$a")
      .matches("(binary)'hello'")
      .matches("(binary)\"hello\"")
      .matches("(binary)<<<EOT\nhello\nEOT")
      .matches("(binary)<<<\"EOT\"\nhello\nEOT")
      .matches("(binary)<<<'EOT'\nhello\nEOT")
      .matches("b'hello'")
      .notMatches("b")
      .notMatches("c'hello'")
      .notMatches("'hello'")
      .matches("B\"hello\"")
      .matches("b<<<EOT\nhello\nEOT")
      .matches("b<<<\"EOT\"\nhello\nEOT")
      .matches("b<<<'EOT'\nhello\nEOT")
      .notMatches("<<<EOT\nhello\nEOT");
  }

}
