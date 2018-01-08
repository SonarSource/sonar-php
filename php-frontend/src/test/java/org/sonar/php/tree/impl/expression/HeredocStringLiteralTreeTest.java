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
package org.sonar.php.tree.impl.expression;

import com.sonar.sslr.api.typed.ActionParser;
import java.util.Collections;
import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.impl.expression.HeredocStringLiteralTreeImpl.HeredocBody;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.HeredocStringLiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

import static org.assertj.core.api.Assertions.assertThat;

public class HeredocStringLiteralTreeTest extends PHPTreeModelTest {

  @Test
  public void test() throws Exception {
    String code = "<<<ABC\nHello $name!{$foo->bar}!\nABC";
    HeredocStringLiteralTree tree = parseHeredoc(code);
    assertThat(tree.openingToken().text()).isEqualTo("<<<ABC\n");
    assertThat(tree.closingToken().text()).isEqualTo("\nABC");
    assertThat(tree.expressions()).hasSize(2);
    assertThat(tree.strings()).hasSize(3);
  }

  @Test
  public void label_with_quotes() throws Exception {
    String code = "<<<\"ABC\"\nHello $name!{$foo->bar}!\nABC";
    HeredocStringLiteralTree tree = parseHeredoc(code);
    assertThat(tree.openingToken().text()).isEqualTo("<<<\"ABC\"\n");
    assertThat(tree.closingToken().text()).isEqualTo("\nABC");
    assertThat(tree.expressions()).hasSize(2);
    assertThat(tree.strings()).hasSize(3);
  }

  @Test
  public void with_double_quotes_inside() throws Exception {
    String code = "<<<ABC\nHello \"John\"!\nABC";
    HeredocStringLiteralTree tree = parseHeredoc(code);
    assertThat(tree.expressions()).hasSize(0);
    assertThat(tree.strings()).hasSize(1);
    assertThat(tree.strings().get(0).value()).isEqualTo("Hello \"John\"!");
  }

  @Test
  public void test_pseudo_comment() throws Exception {
    String code = "<<<EOF\n" +
      "/**/{$a}\n" +
      "EOF";
    HeredocStringLiteralTree tree = parseHeredoc(code);
    assertThat(tree.strings().get(0).value()).isEqualTo("/**/");
  }

  @Test
  public void no_content_token_location() throws Exception {
    String code = "<<<EOD\n"
      + "EOD";
    HeredocStringLiteralTree tree = parseHeredoc(code);

    SyntaxToken openingToken = tree.openingToken();
    assertThat(openingToken.text()).isEqualTo("<<<EOD\n");
    assertThat(openingToken.line()).isEqualTo(1);
    assertThat(openingToken.endLine()).isEqualTo(2);
    assertThat(openingToken.column()).isEqualTo(0);
    assertThat(openingToken.endColumn()).isEqualTo(0);

    assertThat(tree.expressions()).hasSize(0);

    SyntaxToken closingToken = tree.closingToken();
    assertThat(closingToken.text()).isEqualTo("EOD");
    assertThat(closingToken.line()).isEqualTo(2);
    assertThat(closingToken.endLine()).isEqualTo(2);
    assertThat(closingToken.column()).isEqualTo(0);
    assertThat(closingToken.endColumn()).isEqualTo(3);
  }

  @Test
  public void empty_content_token_location() throws Exception {
    String code = "/**/<<<EOD\n"
      + "\n"
      + "EOD";
    HeredocStringLiteralTree tree = parseHeredoc(code);

    SyntaxToken openingToken = tree.openingToken();
    assertThat(openingToken.text()).isEqualTo("<<<EOD\n");
    assertThat(openingToken.line()).isEqualTo(1);
    assertThat(openingToken.endLine()).isEqualTo(2);
    assertThat(openingToken.column()).isEqualTo(4);
    assertThat(openingToken.endColumn()).isEqualTo(0);

    assertThat(tree.expressions()).hasSize(0);

    SyntaxToken closingToken = tree.closingToken();
    assertThat(closingToken.text()).isEqualTo("\nEOD");
    assertThat(closingToken.line()).isEqualTo(2);
    assertThat(closingToken.endLine()).isEqualTo(3);
    assertThat(closingToken.column()).isEqualTo(0);
    assertThat(closingToken.endColumn()).isEqualTo(3);
  }

  @Test
  public void with_content_token_location() throws Exception {
    String code = "/**/<<<EOD\n"
      + "  ABC\n"
      + "  DEF\n"
      + "EOD";
    HeredocStringLiteralTree tree = parseHeredoc(code);

    SyntaxToken openingToken = tree.openingToken();
    assertThat(openingToken.text()).isEqualTo("<<<EOD\n");
    assertThat(openingToken.line()).isEqualTo(1);
    assertThat(openingToken.endLine()).isEqualTo(2);
    assertThat(openingToken.column()).isEqualTo(4);
    assertThat(openingToken.endColumn()).isEqualTo(0);

    assertThat(tree.expressions()).hasSize(0);

    SyntaxToken closingToken = tree.closingToken();
    assertThat(closingToken.text()).isEqualTo("\nEOD");
    assertThat(closingToken.line()).isEqualTo(3);
    assertThat(closingToken.endLine()).isEqualTo(4);
    assertThat(closingToken.column()).isEqualTo(5);
    assertThat(closingToken.endColumn()).isEqualTo(3);
  }

  private HeredocStringLiteralTree parseHeredoc(String code) throws Exception {
    HeredocStringLiteralTree tree = parse(code, Kind.HEREDOC_LITERAL);
    assertThat(tree.is(Kind.HEREDOC_LITERAL)).isTrue();
    return tree;
  }

  @Test
  public void heredoc_body() throws Exception {
    ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.HEREDOC_BODY, 0);
    HeredocBody heredoc = (HeredocBody) parser.parse("Start $name End");
    assertThat(heredoc.expressions()).hasSize(3);
    assertThat(heredoc.childrenIterator().hasNext()).isFalse();
    assertThat(heredoc.expressions().get(0).is(Tree.Kind.HEREDOC_STRING_CHARACTERS)).isTrue();
    assertThat(heredoc.expressions().get(1).is(Tree.Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(heredoc.expressions().get(2).is(Tree.Kind.HEREDOC_STRING_CHARACTERS)).isTrue();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void unsupported_kind() throws Exception {
    HeredocBody tree = new HeredocBody(Collections.emptyList());
    tree.getKind();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void unsupported_accept() throws Exception {
    HeredocBody tree = new HeredocBody(Collections.emptyList());
    tree.accept(new PHPVisitorCheck() {
    });
  }

}
