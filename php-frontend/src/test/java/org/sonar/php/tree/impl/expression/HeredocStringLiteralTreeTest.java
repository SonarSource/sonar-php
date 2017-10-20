/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
package org.sonar.php.tree.impl.expression;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.HeredocStringLiteralTree;

import static org.assertj.core.api.Assertions.assertThat;

public class HeredocStringLiteralTreeTest extends PHPTreeModelTest {

  @Test
  public void test() throws Exception {
    String code = "<<<ABC\nHello $name!{$foo->bar}!\nABC";
    HeredocStringLiteralTree tree = parse(code, Kind.HEREDOC_LITERAL);

    assertThat(expressionToString(tree)).isEqualTo(code);
    assertThat(tree.is(Kind.HEREDOC_LITERAL)).isTrue();
    assertThat(tree.openingToken().text()).isEqualTo("<<<ABC\n");
    assertThat(tree.closingToken().text()).isEqualTo("\nABC");
    assertThat(tree.expressions()).hasSize(2);
    assertThat(tree.strings()).hasSize(3);

  }

  @Test
  public void label_with_quotes() throws Exception {
    String code = "<<<\"ABC\"\nHello $name!{$foo->bar}!\nABC";
    HeredocStringLiteralTree tree = parse(code, Kind.HEREDOC_LITERAL);

    assertThat(expressionToString(tree)).isEqualTo(code);
    assertThat(tree.is(Kind.HEREDOC_LITERAL)).isTrue();
    assertThat(tree.openingToken().text()).isEqualTo("<<<\"ABC\"\n");
    assertThat(tree.closingToken().text()).isEqualTo("\nABC");
    assertThat(tree.expressions()).hasSize(2);
    assertThat(tree.strings()).hasSize(3);
  }

  @Test
  public void with_double_quotes_inside() throws Exception {
    String code = "<<<ABC\nHello \"John\"!\nABC";
    HeredocStringLiteralTree tree = parse(code, Kind.HEREDOC_LITERAL);

    assertThat(expressionToString(tree)).isEqualTo(code);
    assertThat(tree.is(Kind.HEREDOC_LITERAL)).isTrue();
    assertThat(tree.expressions()).hasSize(0);
    assertThat(tree.strings()).hasSize(1);
    assertThat(tree.strings().get(0).value()).isEqualTo("Hello \"John\"!");
  }

  @Test
  public void test_pseudo_comment() throws Exception {
    String code = "<<<EOF\n" +
      "/**/{$a}\n" +
      "EOF";
    HeredocStringLiteralTree tree = parse(code, Kind.HEREDOC_LITERAL);

    assertThat(expressionToString(tree)).isEqualTo(code);
    assertThat(tree.is(Kind.HEREDOC_LITERAL)).isTrue();
    assertThat(tree.strings().get(0).value()).isEqualTo("/**/");
  }

  @Test
  public void empty_content() throws Exception {
    String code = "<<<EOD\nEOD";
    HeredocStringLiteralTree tree = parse(code, Kind.HEREDOC_LITERAL);

    assertThat(expressionToString(tree)).isEqualTo(code);
    assertThat(tree.is(Kind.HEREDOC_LITERAL)).isTrue();
    assertThat(tree.openingToken().text()).isEqualTo("<<<EOD\n");
    assertThat(tree.closingToken().text()).isEqualTo("EOD");
    assertThat(tree.expressions()).hasSize(0);
  }

}
