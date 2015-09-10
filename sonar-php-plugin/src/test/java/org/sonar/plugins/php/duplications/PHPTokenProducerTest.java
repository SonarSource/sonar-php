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
package org.sonar.plugins.php.duplications;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.sonar.duplications.token.Token;
import org.sonar.duplications.token.TokenChunker;
import org.sonar.duplications.token.TokenQueue;
import org.sonar.test.TestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class PHPTokenProducerTest {

  private TokenChunker chunker = PhpTokenProducer.build();

  @Test
  public void shouldIgnoreWhitespaces() {
    assertThat(chunk(" \t\f\n\r"), isTokens());
  }

  @Test
  public void shouldIgnoreEndOfLineComment() {
    assertThat(chunk("// This is a comment"), isTokens());
    assertThat(chunk("// This is a comment \n and_this_is_not"), isTokens(new Token("and_this_is_not", 2, 1)));
    assertThat(chunk("# This is a comment"), isTokens());
    assertThat(chunk("# This is a comment \n and_this_is_not"), isTokens(new Token("and_this_is_not", 2, 1)));
  }

  @Test
  public void shouldIgnoreTraditionalComment() {
    assertThat(chunk("/* This is a comment \n and the second line */"), isTokens());
    assertThat(chunk("/** This is a javadoc \n and the second line */"), isTokens());
    assertThat(chunk("/* this \n comment /* \n // /** ends \n here: */"), isTokens());
  }

  @Test
  public void shouldPreserveIdentifiers() {
    assertThat(chunk("String"), isTokens(new Token("String", 1, 0)));
    assertThat(chunk("i3"), isTokens(new Token("i3", 1, 0)));
    assertThat(chunk("MAX_VALUE"), isTokens(new Token("MAX_VALUE", 1, 0)));
    assertThat(chunk("isLetterOrDigit"), isTokens(new Token("isLetterOrDigit", 1, 0)));

    assertThat(chunk("_"), isTokens(new Token("_", 1, 0)));
    assertThat(chunk("_123_"), isTokens(new Token("_123_", 1, 0)));
    assertThat(chunk("_Field"), isTokens(new Token("_Field", 1, 0)));
    assertThat(chunk("_Field5"), isTokens(new Token("_Field5", 1, 0)));

    assertThat(chunk("i2j"), isTokens(new Token("i2j", 1, 0)));
    assertThat(chunk("from1to4"), isTokens(new Token("from1to4", 1, 0)));
  }

  @Test
  public void shouldPreserverKeywords() {
    assertThat(chunk("public class function"), isTokens(new Token("public", 1, 0), new Token("class", 1, 7), new Token("function", 1, 13)));
  }

  @Test
  public void shouldPreserveBooleanLiterals() {
    assertThat(chunk("true false"), isTokens(new Token("true", 1, 0), new Token("false", 1, 5)));
  }

  @Test
  public void shouldPreserverNullLiteral() {
    assertThat(chunk("null"), isTokens(new Token("null", 1, 0)));
  }

  @Test
  public void shouldNormalizeDecimalIntegerLiteral() {
    assertThat(chunk("543"), isNumericLiteral());
    assertThat(chunk("+543"), isNumericLiteral());
    assertThat(chunk("-543"), isNumericLiteral());
  }

  @Test
  public void shouldNormalizeOctalIntegerLiteral() {
    assertThat(chunk("077"), isNumericLiteral());
    assertThat(chunk("+077"), isNumericLiteral());
    assertThat(chunk("-077"), isNumericLiteral());
  }

  @Test
  public void shouldNormalizeHexIntegerLiteral() {
    assertThat(chunk("0xFF"), isNumericLiteral());
    assertThat(chunk("+0xFF"), isNumericLiteral());
    assertThat(chunk("-0xFF"), isNumericLiteral());

    assertThat(chunk("0XFF"), isNumericLiteral());
    assertThat(chunk("+0XFF"), isNumericLiteral());
    assertThat(chunk("-0XFF"), isNumericLiteral());
  }

  @Test
  public void shouldNormalizeBinaryIntegerLiteral() {
    assertThat(chunk("0b10"), isNumericLiteral());
    assertThat(chunk("+0b10"), isNumericLiteral());
    assertThat(chunk("-0b10"), isNumericLiteral());
  }

  @Test
  public void shouldNormalizeDecimalFloatingPointLiteral() {
    // with dot at the end
    assertThat(chunk("1234."), isNumericLiteral());
    assertThat(chunk("1234.E1"), isNumericLiteral());
    assertThat(chunk("1234.e+1"), isNumericLiteral());
    assertThat(chunk("1234.E-1"), isNumericLiteral());

    // with dot between
    assertThat(chunk("12.34"), isNumericLiteral());
    assertThat(chunk("12.34E1"), isNumericLiteral());
    assertThat(chunk("12.34e+1"), isNumericLiteral());
    assertThat(chunk("12.34E-1"), isNumericLiteral());

    // with dot at the beginning
    assertThat(chunk(".1234"), isNumericLiteral());
    assertThat(chunk(".1234e1"), isNumericLiteral());
    assertThat(chunk(".1234E+1"), isNumericLiteral());
    assertThat(chunk(".1234E-1"), isNumericLiteral());

    // without dot
    assertThat(chunk("1234e1"), isNumericLiteral());
    assertThat(chunk("1234E+1"), isNumericLiteral());
    assertThat(chunk("1234E-1"), isNumericLiteral());

    // with + and - at the beginning
    assertThat(chunk("-1234e1"), isNumericLiteral());
    assertThat(chunk("+1234E+1"), isNumericLiteral());
  }

  @Test
  public void shouldNormalizeCharacterLiterals() {
    assertThat("single character", chunk("'a'"), isStringLiteral());
    assertThat("escaped LF", chunk("'\\n'"), isStringLiteral());
    assertThat("escaped quote", chunk("'\\''"), isStringLiteral());
    assertThat("octal escape", chunk("'\\177'"), isStringLiteral());
    assertThat("unicode escape", chunk("'\\u03a9'"), isStringLiteral());
  }

  @Test
  public void shouldNormalizeStringLiterals() {
    assertThat("regular string", chunk("\"string\""), isStringLiteral());
    assertThat("empty string", chunk("\"\""), isStringLiteral());
    assertThat("escaped LF", chunk("\"\\n\""), isStringLiteral());
    assertThat("escaped double quotes", chunk("\"string, which contains \\\"escaped double quotes\\\"\""), isStringLiteral());
    assertThat("octal escape", chunk("\"string \\177\""), isStringLiteral());
    assertThat("unicode escape", chunk("\"string \\u03a9\""), isStringLiteral());
  }

  @Test
  public void shouldNormalizeHeredocLiterals() {
    assertThat("regular heredoc", chunk("<<<EOT\nThis is a heredoc string \n on multiple lines\nEOT"), isStringLiteral());
    assertThat("wrong heredoc", chunk("<<<EOT\nThis is a wrong heredoc string \n on multiple lines\n EOT"), not(isStringLiteral()));
    assertThat("doublequote heredoc", chunk("<<<\"EOT\"\nThis is a heredoc string \n on multiple lines\nEOT"), isStringLiteral());
  }

  @Test
  public void shouldNormalizeNowdocLiterals() {
    assertThat("regular nowdoc", chunk("<<<'EOT'\nThis is a heredoc string \n on multiple lines\nEOT"), isStringLiteral());
    assertThat("regular nowdoc", chunk("<<<'EOT'\nThis is a heredoc string \n on multiple lines  EOT"), not(isStringLiteral()));
  }

  @Test
  public void shouldPreserveSeparators() {
    assertThat(
      chunk("(){}[];,."),
      isTokens(new Token("(", 1, 0), new Token(")", 1, 1), new Token("{", 1, 2), new Token("}", 1, 3), new Token("[", 1, 4), new Token(
        "]", 1, 5), new Token(";", 1, 6), new Token(",", 1, 7), new Token(".", 1, 8)));
  }

  @Test
  public void shouldPreserveOperators() {
    assertThat(chunk("+="), isTokens(new Token("+", 1, 0), new Token("=", 1, 1)));
    assertThat(chunk("--"), isTokens(new Token("-", 1, 0), new Token("-", 1, 1)));
  }

  @Test
  public void realExamples() {
    assertThat(chunk(TestUtils.getResource("org/sonar/plugins/php/duplications/BigFile.php")).size(), greaterThan(0));
    assertThat(chunk(TestUtils.getResource("org/sonar/plugins/php/duplications/SmallFile.php")).size(), is(44));
  }

  private TokenQueue chunk(File file) {
    Reader reader = null;
    try {
      reader = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
      return chunker.chunk(reader);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(reader);
    }
  }

  private static Matcher<List<Token>> isNumericLiteral() {
    return isTokens(new Token("$NUMBER", 1, 0));
  }

  private static Matcher<List<Token>> isStringLiteral() {
    return isTokens(new Token("$CHARS", 1, 0));
  }

  private static Matcher<List<Token>> isTokens(Token... tokens) {
    return is(Arrays.asList(tokens));
  }

  private List<Token> chunk(String sourceCode) {
    return Lists.newArrayList(chunker.chunk(sourceCode));
  }

}
