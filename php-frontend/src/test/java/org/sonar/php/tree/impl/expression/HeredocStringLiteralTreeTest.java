/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
import org.junit.jupiter.api.Test;
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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class HeredocStringLiteralTreeTest extends PHPTreeModelTest {

  @Test
  void test() throws Exception {
    String code = "<<<ABC\nHello $name!{$foo->bar}!\nABC";
    HeredocStringLiteralTree tree = parseHeredoc(code);
    assertThat(tree.openingToken().text()).isEqualTo("<<<ABC\n");
    assertThat(tree.closingToken().text()).isEqualTo("\nABC");
    assertThat(tree.expressions()).hasSize(2);
    assertThat(tree.strings()).hasSize(3);
  }

  @Test
  void labelWithQuotes() {
    String code = "<<<\"ABC\"\nHello $name!{$foo->bar}!\nABC";
    HeredocStringLiteralTree tree = parseHeredoc(code);
    assertThat(tree.openingToken().text()).isEqualTo("<<<\"ABC\"\n");
    assertThat(tree.closingToken().text()).isEqualTo("\nABC");
    assertThat(tree.expressions()).hasSize(2);
    assertThat(tree.strings()).hasSize(3);
  }

  @Test
  void flexibleHeredocSyntax() {
    String code = "<<<\"ABC\"\n  Hello $name!{$foo->bar}!\n  ABC";
    HeredocStringLiteralTree tree = parseHeredoc(code);
    assertThat(tree.openingToken().text()).isEqualTo("<<<\"ABC\"\n");
    assertThat(tree.closingToken().text()).isEqualTo("\n  ABC");
    assertThat(tree.expressions()).hasSize(2);
    assertThat(tree.strings()).hasSize(3);
  }

  @Test
  void withDoubleQuotesInside() {
    String code = "<<<ABC\nHello \"John\"!\nABC";
    HeredocStringLiteralTree tree = parseHeredoc(code);
    assertThat(tree.expressions()).isEmpty();
    assertThat(tree.strings()).hasSize(1);
    assertThat(tree.strings().get(0).value()).isEqualTo("Hello \"John\"!");
  }

  @Test
  void testPseudoComment() {
    String code = "<<<EOF\n" +
      "/**/{$a}\n" +
      "EOF";
    HeredocStringLiteralTree tree = parseHeredoc(code);
    assertThat(tree.strings().get(0).value()).isEqualTo("/**/");
  }

  @Test
  void noContentTokenLocation() {
    String code = "<<<EOD\n"
      + "EOD";
    HeredocStringLiteralTree tree = parseHeredoc(code);

    SyntaxToken openingToken = tree.openingToken();
    assertThat(openingToken.text()).isEqualTo("<<<EOD\n");
    assertThat(openingToken.line()).isEqualTo(1);
    assertThat(openingToken.endLine()).isEqualTo(2);
    assertThat(openingToken.column()).isZero();
    assertThat(openingToken.endColumn()).isZero();

    assertThat(tree.expressions()).isEmpty();

    SyntaxToken closingToken = tree.closingToken();
    assertThat(closingToken.text()).isEqualTo("EOD");
    assertThat(closingToken.line()).isEqualTo(2);
    assertThat(closingToken.endLine()).isEqualTo(2);
    assertThat(closingToken.column()).isZero();
    assertThat(closingToken.endColumn()).isEqualTo(3);
  }

  @Test
  void emptyContentTokenLocation() {
    String code = "/**/<<<EOD\n"
      + "\n"
      + "EOD";
    HeredocStringLiteralTree tree = parseHeredoc(code);

    SyntaxToken openingToken = tree.openingToken();
    assertThat(openingToken.text()).isEqualTo("<<<EOD\n");
    assertThat(openingToken.line()).isEqualTo(1);
    assertThat(openingToken.endLine()).isEqualTo(2);
    assertThat(openingToken.column()).isEqualTo(4);
    assertThat(openingToken.endColumn()).isZero();

    assertThat(tree.expressions()).isEmpty();

    SyntaxToken closingToken = tree.closingToken();
    assertThat(closingToken.text()).isEqualTo("\nEOD");
    assertThat(closingToken.line()).isEqualTo(2);
    assertThat(closingToken.endLine()).isEqualTo(3);
    assertThat(closingToken.column()).isZero();
    assertThat(closingToken.endColumn()).isEqualTo(3);
  }

  @Test
  void withContentTokenLocation() {
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
    assertThat(openingToken.endColumn()).isZero();

    assertThat(tree.expressions()).isEmpty();

    SyntaxToken closingToken = tree.closingToken();
    assertThat(closingToken.text()).isEqualTo("\nEOD");
    assertThat(closingToken.line()).isEqualTo(3);
    assertThat(closingToken.endLine()).isEqualTo(4);
    assertThat(closingToken.column()).isEqualTo(5);
    assertThat(closingToken.endColumn()).isEqualTo(3);
  }

  @Test
  void parseBackslash() {
    String code = "<<<ABC\n\\\nABC";
    HeredocStringLiteralTree tree = parseHeredoc(code);
    assertThat(tree.strings().get(0).value()).isEqualTo("\\");
  }

  private HeredocStringLiteralTree parseHeredoc(String code) {
    HeredocStringLiteralTree tree = parse(code, Kind.HEREDOC_LITERAL);
    assertThat(tree.is(Kind.HEREDOC_LITERAL)).isTrue();
    return tree;
  }

  @Test
  void heredocBody() {
    ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.HEREDOC_BODY, 0);
    HeredocBody heredoc = (HeredocBody) parser.parse("Start $name End");
    assertThat(heredoc.expressions()).hasSize(3);
    assertThat(heredoc.childrenIterator().hasNext()).isFalse();
    assertThat(heredoc.expressions().get(0).is(Tree.Kind.HEREDOC_STRING_CHARACTERS)).isTrue();
    assertThat(heredoc.expressions().get(1).is(Tree.Kind.VARIABLE_IDENTIFIER)).isTrue();
    assertThat(heredoc.expressions().get(2).is(Tree.Kind.HEREDOC_STRING_CHARACTERS)).isTrue();
  }

  @Test
  void unsupportedKind() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> {
      HeredocBody tree = new HeredocBody(Collections.emptyList());
      tree.getKind();
    });
  }

  @Test
  void unsupportedAccept() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> {
      HeredocBody tree = new HeredocBody(Collections.emptyList());
      tree.accept(new PHPVisitorCheck() {
      });
    });
  }

}
