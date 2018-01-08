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
package org.sonar.php.parser.statement;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.sonar.php.utils.Assertions.assertThat;

public class UseStatementTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.USE_STATEMENT)
      .matches("use My\\Full\\Name as alias;")
      .matches("use const My\\Full\\Name as alias;")
      .matches("use function My\\Full\\Name as alias;")
      .matches("use My\\Full\\Name1, My\\Full\\Name2;")
      .matches("use function foo;")
      .notMatches("use function My\\Full\\Name1 as func, const My\\Full\\Name2;");
  }

  @Test
  public void group_use() {
    assertThat(Kind.GROUP_USE_STATEMENT)
      .matches("use My\\Project\\{Class1, Class2};")
      .matches("use const My\\Full\\{Name1 as A, Name2 as B};")
      .matches("use function My\\Full\\{Name as alias};")
      .matches("use \\My\\Full\\{Name1, Name2};")
      .matches("use My\\Full\\{const Name1, function Name2, const Name3 as C};")

      // matches but it's not a valid PHP code
      .matches("use const My\\Full\\{const Name1};")
      .matches("use function My\\Full\\{function Name1};")
    ;
  }

}
