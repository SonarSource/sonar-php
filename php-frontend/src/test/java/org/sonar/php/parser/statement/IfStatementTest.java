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

import static org.sonar.php.utils.Assertions.assertThat;

public class IfStatementTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.IF_STATEMENT)
        // fixme (Lena) : replace all conditions by parenthesised expressions
      .matches("if $a {}")
      .matches("if $a {} elseif $a {}")
      .matches("if $a {} elseif $a {} elseif $a {}")
      .matches("if $a {} elseif $a {} else {}")
      .matches("if $a {} else {}")

      .matches("if $a : endif;")
      .matches("if $a : elseif $a: endif;")
      .matches("if $a : elseif $a: else: endif;")
      .matches("if $a : else: endif;")


      .notMatches("if $a : {}")

    ;
  }

  @Test
  public void realLife() throws Exception {
    assertThat(PHPLexicalGrammar.IF_STATEMENT)
        //fixme (Lena): should match
      .notMatches("if (\"#$a\") {\n $x = ''; }");
  }
}
