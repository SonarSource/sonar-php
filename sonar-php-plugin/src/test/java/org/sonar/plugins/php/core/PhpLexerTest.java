/*
 * Sonar PHP Plugin
 * Copyright (C) 2010 Codehaus Sonar Plugins
 * dev@sonar.codehaus.org
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
package org.sonar.plugins.php.core;

import com.google.common.base.Charsets;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Lexer;
import org.junit.Test;

import java.util.List;

import static com.sonar.sslr.test.lexer.LexerMatchers.hasComment;
import static com.sonar.sslr.test.lexer.LexerMatchers.hasToken;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class PhpLexerTest {

  private final PhpParserConfiguration conf = PhpParserConfiguration.builder()
      .setCharset(Charsets.UTF_8)
      .build();
  private final Lexer lexer = PhpLexer.create(conf);

  @Test
  public void single_line_shell_comment() {
    assertThat(lexer.lex("#foo"), hasComment("#foo"));
    assertThat(lexer.lex("#foo?>"), hasComment("#foo"));
    assertThat(lexer.lex("#foo\r"), hasComment("#foo"));
    assertThat(lexer.lex("#foo\n"), hasComment("#foo"));
    assertThat(lexer.lex("#bonjour\nIDENTIFIER"), hasComment("#bonjour"));
    assertThat(lexer.lex("#foo?\n"), hasComment("#foo?"));
  }

  @Test
  public void single_line_comment() {
    assertThat(lexer.lex("//foo"), hasComment("//foo"));
    assertThat(lexer.lex("//foo?>"), hasComment("//foo"));
    assertThat(lexer.lex("//foo\r"), hasComment("//foo"));
    assertThat(lexer.lex("//foo\n"), hasComment("//foo"));
    assertThat(lexer.lex("//bonjour\nIDENTIFIER"), hasComment("//bonjour"));
    assertThat(lexer.lex("//foo?\n"), hasComment("//foo?"));
  }

  @Test
  public void multi_line_comment() {
    assertThat(lexer.lex("/**/"), hasComment("/**/"));
    assertThat(lexer.lex("/*foo*/"), hasComment("/*foo*/"));
    assertThat(lexer.lex("/*foo\nbar*/"), hasComment("/*foo\nbar*/"));
    assertThat(lexer.lex("/*foo?>bar*/"), hasComment("/*foo?>bar*/"));
    assertThat(lexer.lex("/*test/* /**/"), hasComment("/*test/* /**/"));
    assertThat(lexer.lex("/*test1\ntest2\ntest3*/"), hasComment("/*test1\ntest2\ntest3*/"));
  }

  @Test
  public void blackhole() {
    assertThat(lexer.lex(" ")).hasSize(1);
    assertThat(lexer.lex("\t")).hasSize(1);
    assertThat(lexer.lex("\r")).hasSize(1);
    assertThat(lexer.lex("\n")).hasSize(1);
    assertThat(lexer.lex("\r \n \t")).hasSize(1);
  }

  @Test
  public void single_quote_string() {
    assertThat(lexer.lex("''"), hasToken("''", PhpLexerType.STRING));
    assertThat(lexer.lex("'foo'"), hasToken("'foo'", PhpLexerType.STRING));
    assertThat(lexer.lex("'foo\\\\bar'"), hasToken("'foo\\\\bar'", PhpLexerType.STRING));
    assertThat(lexer.lex("'foo\\'bar'"), hasToken("'foo\\'bar'", PhpLexerType.STRING));
    assertThat(lexer.lex("'foo\nbar'"), hasToken("'foo\nbar'", PhpLexerType.STRING));
  }

  @Test
  public void double_quote_string() {
    assertThat(lexer.lex("\"\""), hasToken("\"\"", PhpLexerType.STRING));
    assertThat(lexer.lex("\"foo\""), hasToken("\"foo\"", PhpLexerType.STRING));
    assertThat(lexer.lex("\"foo\\\\bar\""), hasToken("\"foo\\\\bar\"", PhpLexerType.STRING));
    assertThat(lexer.lex("\"foo\\'bar\""), hasToken("\"foo\\'bar\"", PhpLexerType.STRING));
    assertThat(lexer.lex("\"foo\nbar\""), hasToken("\"foo\nbar\"", PhpLexerType.STRING));
    assertThat(lexer.lex("\"a string #bonjour comment allez-vous?\""), not(hasComment("#bonjour comment allez-vous?\"")));
  }

  @Test
  public void heredoc_and_newdoc_string() {
    assertThat(lexer.lex("<<<F\nF"), hasToken("<<<F\nF", PhpLexerType.STRING));
    assertThat(lexer.lex("<<<FOO\nFOO"), hasToken("<<<FOO\nFOO", PhpLexerType.STRING));
    assertThat(lexer.lex("<<<'FOO'\nFOO"), hasToken("<<<'FOO'\nFOO", PhpLexerType.STRING));
    assertThat(lexer.lex("<<<\"FOO\"\nFOO"), hasToken("<<<\"FOO\"\nFOO", PhpLexerType.STRING));

    assertThat(lexer.lex("<<<FOO\nFOO;"), hasToken("<<<FOO\nFOO", PhpLexerType.STRING));
    assertThat(lexer.lex("<<<FOO\n FOO\nFOO"), hasToken("<<<FOO\n FOO\nFOO", PhpLexerType.STRING));
    assertThat(lexer.lex("<<<FOO\nFOO bar\nFOO"), hasToken("<<<FOO\nFOO bar\nFOO", PhpLexerType.STRING));
    assertThat(lexer.lex("<<<FOO\nFOO\n"), hasToken("<<<FOO\nFOO", PhpLexerType.STRING));
    assertThat(lexer.lex("<<<FOO\nFOO;\n"), hasToken("<<<FOO\nFOO", PhpLexerType.STRING));
    assertThat(lexer.lex("<<<FOO\nFOO\r"), hasToken("<<<FOO\nFOO", PhpLexerType.STRING));
    assertThat(lexer.lex("<<<FOO\nFOO;\r"), hasToken("<<<FOO\nFOO", PhpLexerType.STRING));

    assertThat(lexer.lex("<<<FOO BAR\nFOO"), not(hasToken("<<<FOO BAR\nFOO", PhpLexerType.STRING)));
  }

  @Test
  public void lexRealFile() {
    List<Token> tokens = lexer.lex(this.getClass().getResource("/Math2.php"));
    assertThat(tokens.size()).isEqualTo(1206);
  }

}
