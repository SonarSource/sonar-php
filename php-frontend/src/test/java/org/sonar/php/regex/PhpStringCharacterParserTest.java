/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.Test;
import org.sonar.php.tree.impl.expression.LiteralTreeImpl;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonarsource.analyzer.commons.regex.CharacterParser;
import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.ast.SourceCharacter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PhpStringCharacterParserTest {

  @Test
  public void single_quoted() {
    assertThat(chars(singleQuoted(""))).isEmpty();
    assertThat(chars(singleQuoted("abc"))).containsExactly('a', 'b', 'c');
    assertThat(chars(singleQuoted("\\'\\x\\\"\\\\"))).containsExactly('\'', '\\', 'x', '\\', '"', '\\');

    assertThat(charTexts(singleQuoted("\\'\\x"))).containsExactly("\\'", "\\", "x");

    assertThat(chars(singleQuoted("\\"))).containsExactly('\\');

    assertThat(chars(singleQuoted("\\n"))).containsExactly('\\', 'n');
    assertThat(chars(singleQuoted("\\r"))).containsExactly('\\', 'r');
    assertThat(chars(singleQuoted("\\t"))).containsExactly('\\', 't');
    assertThat(chars(singleQuoted("\\v"))).containsExactly('\\', 'v');
    assertThat(chars(singleQuoted("\\e"))).containsExactly('\\', 'e');
    assertThat(chars(singleQuoted("\\f"))).containsExactly('\\', 'f');
    assertThat(chars(singleQuoted("\\$"))).containsExactly('\\', '$');

    assertThat(chars(singleQuoted("\\x41"))).containsExactly('\\', 'x', '4', '1');

    assertThat(chars(singleQuoted("\\102"))).containsExactly('\\', '1', '0', '2');

    assertThat(chars(singleQuoted("\\u{0043}"))).containsExactly('\\', 'u', '{', '0', '0', '4', '3', '}');
  }

  @Test
  public void double_quoted() {
    assertThat(chars(doubleQuoted(""))).isEmpty();
    assertThat(chars(doubleQuoted("abc"))).containsExactly('a', 'b', 'c');
    assertThat(chars(doubleQuoted("\\'\\x\\\""))).containsExactly('\\', '\'', '\\', 'x', '"');

    assertThat(chars(doubleQuoted("\\"))).containsExactly('\\');

    assertThat(chars(doubleQuoted("\\\\n"))).containsExactly('\\', 'n');
    assertThat(chars(doubleQuoted("\\n"))).containsExactly('\n');
    assertThat(chars(doubleQuoted("\\r"))).containsExactly('\r');
    assertThat(chars(doubleQuoted("\\t"))).containsExactly('\t');
    assertThat(chars(doubleQuoted("\\v"))).containsExactly('\u000b');
    assertThat(chars(doubleQuoted("\\e"))).containsExactly('\u001b');
    assertThat(chars(doubleQuoted("\\f"))).containsExactly('\f');
    assertThat(chars(doubleQuoted("\\$"))).containsExactly('$');

    assertThat(chars(doubleQuoted("\\x41"))).containsExactly('A');
    assertThat(chars(doubleQuoted("\\xx"))).containsExactly('\\', 'x', 'x');
    assertThat(chars(doubleQuoted("\\xa"))).containsExactly('\n');

    assertThat(chars(doubleQuoted("\\102"))).containsExactly('B');

    assertThat(chars(doubleQuoted("\\u{0043}"))).containsExactly('C');
    assertThat(chars(doubleQuoted("\\ux"))).containsExactly('\\', 'u', 'x');
  }

  @Test
  public void getCurrent() {
    PhpRegexSource source = regexSource("ab", '"');
    CharacterParser parser = PhpStringCharacterParser.forSingleQuotedString(source);
    assertThat(parser.getCurrent().getCharacter()).isEqualTo('a');
    parser.moveNext();
    assertThat(parser.getCurrent().getCharacter()).isEqualTo('b');
    parser.moveNext();
    assertThatThrownBy(parser::getCurrent).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  public void resetTo() {
    PhpRegexSource source = regexSource("ab", '"');
    CharacterParser parser = PhpStringCharacterParser.forSingleQuotedString(source);
    assertThat(parser.getCurrent().getCharacter()).isEqualTo('a');
    parser.moveNext();
    assertThat(parser.getCurrent().getCharacter()).isEqualTo('b');
    parser.resetTo(0);
    assertThat(parser.getCurrent().getCharacter()).isEqualTo('a');
  }

  private Stream<Character> chars(List<SourceCharacter> sourceCharacters) {
    return sourceCharacters.stream().map(SourceCharacter::getCharacter);
  }

  private Stream<String> charTexts(List<SourceCharacter> sourceCharacters) {
    return sourceCharacters.stream().map(SourceCharacter::getText);
  }

  private List<SourceCharacter> singleQuoted(String str) {
    return sourceCharacters(str, PhpStringCharacterParser::forSingleQuotedString, '\'');
  }

  private List<SourceCharacter> doubleQuoted(String str) {
    return sourceCharacters(str, PhpStringCharacterParser::forDoubleQuotedString, '"');
  }

  private List<SourceCharacter> sourceCharacters(String str, Function<RegexSource, CharacterParser> parserFunction, char quote) {
    PhpRegexSource source = regexSource(str, quote);
    CharacterParser parser = parserFunction.apply(source);
    List<SourceCharacter> characters = new ArrayList<>();
    while (parser.isNotAtEnd()) {
      characters.add(parser.getCurrent());
      parser.moveNext();
    }
    return characters;
  }

  private PhpRegexSource regexSource(String str, char quote) {
    String tokenValue = quote + "/" + str + "/" + quote;
    SyntaxToken token = new InternalSyntaxToken(1, 1, tokenValue, Collections.emptyList(), 0, false);
    LiteralTree literal = new LiteralTreeImpl(Tree.Kind.REGULAR_STRING_LITERAL, token);
    return new PhpRegexSource(literal);
  }
}
