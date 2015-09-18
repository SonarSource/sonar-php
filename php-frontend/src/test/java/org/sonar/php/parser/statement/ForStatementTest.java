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
package org.sonar.php.parser.statement;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.RuleTest;

import static org.sonar.php.utils.Assertions.assertThat;

public class ForStatementTest extends RuleTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.FOR_STATEMENT)
        //todo (Lena) improve these tests with more adequate expressions
      .matches("for ($a; $b; $c) {}")
      .matches("for ($a1, $a2;  $b1, $b2;  $c1, $c2) {}")
      .matches("for ($a; ; $c) {}")
      .matches("for (; ; ) {}")
      .matches("for (; ; ): {} {} endfor;")
      .matches("for (; ; ): {} endfor;")
      .matches("for (; ; ): endfor;");
  }
}
