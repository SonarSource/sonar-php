/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.php.regex;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.php.api.visitors.LocationInFile;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.RegexParser;
import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.SyntaxError;
import org.sonarsource.analyzer.commons.regex.ast.CharacterTree;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.NonCapturingGroupTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.SequenceTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.php.regex.RegexParserTestUtils.assertKind;
import static org.sonar.php.regex.RegexParserTestUtils.assertSuccessfulParse;
import static org.sonar.php.regex.RegexParserTestUtils.makeSource;
import static org.sonar.php.regex.RegexParserTestUtils.parseRegex;

class PhpAnalyzerRegexSourceTest {

  @Test
  // TODO: Extend test with exact syntax error location check
  void invalidRegex() {
    RegexSource source = makeSource("'/+/'");
    RegexParseResult result = new RegexParser(source, new FlagSet()).parse();

    assertThat(result.getSyntaxErrors()).isNotEmpty();
  }

  @Test
  void testToFewDelimiters() {
    assertThatThrownBy(() -> makeSource("'/'"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Regular expression does not contain delimiters");
  }

  @Test
  void testNonStringLiteral() {
    assertThatThrownBy(() -> makeSource("1"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only string literals allowed");
  }

  @Test
  void testStringLiteral() {
    RegexTree regex = assertSuccessfulParse("'/a\\nb/'"); // <?php foo('/a\nb/');
    assertKind(RegexTree.Kind.SEQUENCE, regex);
    List<RegexTree> items = ((SequenceTree) regex).getItems();
    assertThat(items).hasSize(3);

    assertCharacter('a', items.get(0));
    assertCharacter('\n', items.get(1));
    assertCharacter('b', items.get(2));

    assertLocation(3, 2, 3, items.get(0));
    assertLocation(3, 3, 5, items.get(1));
    assertLocation(3, 5, 6, items.get(2));
  }

  @Test
  void multilineStringLiteral() {
    RegexTree regex = assertSuccessfulParse("'/a\nbc\r\nde/'");
    assertKind(RegexTree.Kind.SEQUENCE, regex);
    List<RegexTree> items = ((SequenceTree) regex).getItems();

    assertCharacterLocation(items.get(0), 'a', 3, 2, 3);
    assertCharacterLocation(items.get(2), 'b', 4, 0, 1);
    assertCharacterLocation(items.get(3), 'c', 4, 1, 2);
    assertCharacterLocation(items.get(6), 'd', 5, 0, 1);
  }

  @Test
  void singleQuoteVsDoubleQuote() {
    RegexParseResult singleQuoted = new RegexParser(makeSource("'/\\u{0041}/'"), new FlagSet()).parse();
    assertThat(singleQuoted.getSyntaxErrors()).extracting(SyntaxError::getMessage).containsExactly("Expected hexadecimal digit, but found '{'");

    RegexParseResult doubleQuoted = new RegexParser(makeSource("\"/\\u{0041}/\""), new FlagSet()).parse();
    assertThat(doubleQuoted.getSyntaxErrors()).isEmpty();
    assertThat(doubleQuoted.getResult().kind()).isEqualTo(RegexTree.Kind.CHARACTER);
    assertThat(((CharacterTree) doubleQuoted.getResult()).characterAsString()).isEqualTo("A");
  }

  @Test
  void testStringLiteralWithBracketDelimiters() {
    RegexTree regex = assertSuccessfulParse("'[a]'");
    assertKind(RegexTree.Kind.CHARACTER, regex);
    assertCharacter('a', regex);
    assertLocation(3, 2, 3, regex);
  }

  @Test
  void phpLiteralEscapeSequence() {
    RegexTree regex = assertSuccessfulParse("'/a\\\\\\\\b/'");
    assertKind(RegexTree.Kind.SEQUENCE, regex);
    List<RegexTree> items = ((SequenceTree) regex).getItems();
    assertThat(items).allMatch(t -> t.is(RegexTree.Kind.CHARACTER))
      .extracting(t -> ((CharacterTree) t).characterAsString())
      .containsExactly("a", "\\", "b");
    assertLocation(3, 3, 7, items.get(1));
  }

  @Test
  void testLeadingWhitespaceBeforeDelimiter() {
    assertCharacterLocation(assertSuccessfulParse("'    /a/'"), 'a', 3, 6, 7);
    assertCharacterLocation(assertSuccessfulParse("'\n /a/'"), 'a', 4, 2, 3);
    assertCharacterLocation(assertSuccessfulParse("'\r\n\n\r/a/'"), 'a', 3 + 3, 1, 2);
    assertThatThrownBy(() -> parseRegex("'    '")).hasMessageContaining("does not contain delimiters");
  }

  @Test
  void testRecursivePattern() {
    RegexTree regex = assertSuccessfulParse("'/(?R)/'");
    assertKind(RegexTree.Kind.NON_CAPTURING_GROUP, regex);
    assertThat(((NonCapturingGroupTree) regex).getElement()).isNull();

    regex = assertSuccessfulParse("'/(?:R)/'");
    assertKind(RegexTree.Kind.NON_CAPTURING_GROUP, regex);
    assertThat(((NonCapturingGroupTree) regex).getElement()).isNotNull();
  }

  @Test
  void testConditionalSubpatternsWithToManyAlternatives() {
    RegexParseResult regex = parseRegex("'/(?(1)ab|cd|ef)/'");
    assertThat(regex.getSyntaxErrors()).isNotEmpty();
  }

  @Test
  void testConditionalSubpatternsWithInvalidCondition() {
    RegexParseResult regex = parseRegex("'/(?(1|2)ab|cd|ef)/'");
    assertThat(regex.getSyntaxErrors()).isNotEmpty();
  }

  @Test
  void testLocationOnRegexOpener() {
    RegexParseResult regex = parseRegex("'/(?(1|2)ab|cd|ef)/'");
    RegexSyntaxElement openingQuote = regex.openingQuote();
    LocationInFile locationInFile = ((PhpAnalyzerRegexSource) openingQuote.getSource()).locationInFileFor(openingQuote.getRange());
    assertLocation(3, 0, 1, locationInFile);
  }

  private static void assertCharacterLocation(RegexTree tree, char expected, int line, int startLineOffset, int endLineOffset) {
    assertKind(RegexTree.Kind.CHARACTER, tree);
    assertThat((char) ((CharacterTree) tree).codePointOrUnit()).isEqualTo(expected);
    assertLocation(line, startLineOffset, endLineOffset, tree);
  }

  private static void assertCharacter(char expected, RegexTree tree) {
    assertKind(RegexTree.Kind.CHARACTER, tree);
    assertThat(((CharacterTree) tree).codePointOrUnit()).isEqualTo(expected);
  }

  private static void assertLocation(int line, int startLineOffset, int endLineOffset, RegexTree tree) {
    LocationInFile location = ((PhpAnalyzerRegexSource) tree.getSource()).locationInFileFor(tree.getRange());
    assertLocation(line, startLineOffset, endLineOffset, location);
  }

  private static void assertLocation(int line, int startLineOffset, int endLineOffset, LocationInFile location) {
    assertThat(location.startLine()).withFailMessage(String.format("Expected line to be '%d' but got '%d'", line, location.startLine())).isEqualTo(line);
    assertThat(location.endLine()).withFailMessage(String.format("Expected line to be '%d' but got '%d'", line, location.endLine())).isEqualTo(line);
    assertThat(location.startLineOffset()).withFailMessage(String.format("Expected start character to be '%d' but got '%d'", startLineOffset, location.startLineOffset()))
      .isEqualTo(startLineOffset);
    assertThat(location.endLineOffset()).withFailMessage(String.format("Expected end character to be '%d' but got '%d'", endLineOffset, location.endLineOffset()))
      .isEqualTo(endLineOffset);
  }
}
