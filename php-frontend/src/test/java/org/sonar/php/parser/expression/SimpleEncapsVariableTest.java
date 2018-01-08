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
package org.sonar.php.parser.expression;

import static org.sonar.php.utils.Assertions.assertThat;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

public class SimpleEncapsVariableTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.SIMPLE_ENCAPS_VARIABLE)
      .matches("$variable")
      .matches("$variable [ offset ]")
      .matches("$variable -> identifier");
  }

  @Test
  public void test_real_life() {
    assertThat(PHPLexicalGrammar.SIMPLE_ENCAPS_VARIABLE)
      .matches("$foo[0]")
      .matches("$foo[identifier]")
      .matches("$foo[$variable]");

  }

  @Test
  public void nok() throws Exception {
    assertThat(PHPLexicalGrammar.SIMPLE_ENCAPS_VARIABLE)
      .notMatches("$var[test()]")
      .notMatches("$var[{$test}]")
      .notMatches("$var[$$test]");
  }

}
